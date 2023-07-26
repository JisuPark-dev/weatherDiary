package zerobase.weather.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.tomcat.jni.Local;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import zerobase.weather.domain.Diary;
import zerobase.weather.dto.ErrorResponse;
import zerobase.weather.exception.WeatherException;
import zerobase.weather.repository.DiaryRepository;
import zerobase.weather.type.ErrorCode;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static zerobase.weather.type.ErrorCode.CANNOT_GET_WEATHER_DATA_FROM_API;
import static zerobase.weather.type.ErrorCode.WEATHER_DATA_PARSING_ERROR;

@Service
@Slf4j
@RequiredArgsConstructor
public class DiaryService {

    private final DiaryRepository diaryRepository;

    @Value("${openweathermap.key}")
    private String apiKey;

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void createDiary(LocalDate date, String text) {
        // weather 정보 받아오기
        String jsonString = getWeatherString();
        log.info(jsonString);
        // 정보 파싱하기
        Map<String, Object> parseWeather = parseWeather(jsonString);
        // 날씨 값 + 일기 값 db에 저장하기
        saveDiary(date, text, parseWeather);
    }

    @Transactional(readOnly = true)
    public List<Diary> readDiary(LocalDate date) {
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
        diaryRepository.deleteAllByDate(date);
    }

    private String getWeatherString() {
        String apiUrl = "https://api.openweathermap.org/data/2.5/weather?q=seoul&appid="+apiKey;
        log.info(apiUrl);
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
            throw new WeatherException(CANNOT_GET_WEATHER_DATA_FROM_API);
        }
    }

    private Map<String, Object> parseWeather(String jsonString) throws WeatherException {
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject;

        try {
            jsonObject = (JSONObject) jsonParser.parse(jsonString);
        } catch (ParseException e) {
            throw new WeatherException(WEATHER_DATA_PARSING_ERROR);
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

    private void saveDiary(LocalDate date, String text, Map<String, Object> parseWeather) {
        Diary nowDiary = Diary.builder()
                .weather(parseWeather.get("main").toString())
                .icon(parseWeather.get("icon").toString())
                .temperature((Double) parseWeather.get("temp"))
                .text(text)
                .date(date)
                .build();
        diaryRepository.save(nowDiary);
    }
}
