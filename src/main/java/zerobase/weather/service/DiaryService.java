package zerobase.weather.service;

import lombok.RequiredArgsConstructor;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import zerobase.weather.WeatherApplication;
import zerobase.weather.domain.DateWeather;
import zerobase.weather.domain.Diary;
import zerobase.weather.exception.WeatherException;
import zerobase.weather.repository.DateWeatherRepository;
import zerobase.weather.repository.DiaryRepository;
import zerobase.weather.type.ErrorCode;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
@RequiredArgsConstructor
public class DiaryService {

    private final DiaryRepository diaryRepository;
    private final DateWeatherRepository dateWeatherRepository;

    @Value("${openweathermap.key}")
    private String apiKey;

    // 로거 생성
    private static final Logger logger = LoggerFactory.getLogger(WeatherApplication.class);

    @Transactional
    @Scheduled(cron = "0 0 1 * * *")
    // 매일 오전 1시에 그 날의 날씨 정보로서 저장
    // -> 이제 날씨일기 작성시 매번 api 호출하지 않고 저장된 날씨정보로 부터 꺼내서 사용
    public void saveWeatherDate() {
        logger.info("오늘도 날씨 데이터 잘 가져옴");
        dateWeatherRepository.save(getWeatherFromApi());
    }

    private DateWeather getWeatherFromApi() {
        // weather 정보 받아오기
        String jsonString = getWeatherString();
        logger.info(jsonString);
        // 정보 파싱하기
        Map<String, Object> parseWeather = parseWeather(jsonString);
        // date_weather에 값 저장하기
        return DateWeather.builder()
                .date(LocalDate.now())
                .weather(parseWeather.get("main").toString())
                .icon(parseWeather.get("icon").toString())
                .temperature((Double)parseWeather.get("temp"))
                .build();
    }


    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void createDiary(LocalDate date, String text) {
        logger.info("started to created diary");
        // 날씨 데이터 가져오기
        DateWeather dateWeather = getDateWeather(date);
        // 날씨 값 + 일기 값 db에 저장하기
        saveDiary(date, text, dateWeather);
        logger.info("end to created diary");
    }

    private DateWeather getDateWeather(LocalDate date) {
        List<DateWeather> dateWeatherFromDB = dateWeatherRepository.findAllByDate(date);
        if (dateWeatherFromDB.size() == 0) {
            // 새로 api에서 날씨 정보를 가져와야 한다.
            // 정책 상 현재 날씨를 가져오도록 한다.
            return getWeatherFromApi();
        }else{
            return dateWeatherFromDB.get(0);
        }
    }

    @Transactional(readOnly = true)
    public List<Diary> readDiary(LocalDate date) {
        logger.debug("read diary");
        if (date.isAfter(LocalDate.ofYearDay(2050, 1))) {
            throw new WeatherException(ErrorCode.DATE_IS_TOO_FAR_IN_FUTURE);
        }
        return diaryRepository.findAllByDate(date);
    }

    @Transactional(readOnly = true)
    public List<Diary> readDiaries(LocalDate startDate, LocalDate endDate) {
        return diaryRepository.findALlByDateBetween(startDate, endDate);
    }

    public void updateDiary(LocalDate date, String text) {
        Diary diary = diaryRepository.getFirstByDate(date);
        diary.setText(text);
        diaryRepository.save(diary);
    }

    public void deleteDiary(LocalDate date) {
        logger.error("delete diary");
        diaryRepository.deleteAllByDate(date);
    }

    private String getWeatherString() {
        String apiUrl = "https://api.openweathermap.org/data/2.5/weather?q=seoul&appid="+apiKey;
        logger.info(apiUrl);
        try {
            URL url = new URL(apiUrl);
            // 해당 url로 http 연결 생성
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            // 요청 방식을 GET으로 설정
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();
            BufferedReader br;
            if (responseCode == 200) {
                // 네트워크 연결로부터 데이터를 읽어들이려면, Socket 클래스의
                // getInputStream() 메서드를 사용하여 InputStream을 얻을 수 있습니다.
                // InputStreamReader와 BufferedReader로 이 스트림에서 데이터를 읽어옵니다.
                br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            }else{
                br = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
            }

            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = br.readLine()) != null) {
                response.append(inputLine);
            }
            br.close();
            return response.toString();
        } catch (Exception e) {
            throw new WeatherException(ErrorCode.CANNOT_GET_WEATHER_DATA_FROM_API);
        }
    }

    private Map<String, Object> parseWeather(String jsonString) throws WeatherException {
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject;

        try {
            jsonObject = (JSONObject) jsonParser.parse(jsonString);
        } catch (ParseException e) {
            throw new WeatherException(ErrorCode.WEATHER_DATA_PARSING_ERROR);
        }
        Map<String, Object> resultMap = new HashMap<>();

        JSONObject mainData = (JSONObject) jsonObject.get("main");
        resultMap.put("temp", mainData.get("temp"));
        JSONArray weatherArray = (JSONArray) jsonObject.get("weather");
        JSONObject weatherData = (JSONObject) weatherArray.get(0);
        resultMap.put("main", weatherData.get("main"));
        resultMap.put("icon", weatherData.get("icon"));
        return resultMap;
    }

    private void saveDiary(LocalDate date, String text, DateWeather dateWeather) {
        Diary nowDiary = Diary.builder()
                .weather(dateWeather.getWeather())
                .icon(dateWeather.getIcon())
                .temperature(dateWeather.getTemperature())
                .text(text)
                .date(date)
                .build();
        diaryRepository.save(nowDiary);
    }
}
