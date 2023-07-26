package zerobase.weather.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    WEATHER_DATA_PARSING_ERROR("날씨 데이터 파싱이 되지 않습니다."),
    CANNOT_GET_WEATHER_DATA_FROM_API("날씨 데이터를 api로부터 가져오지 못했습니다."),
    INTERNAL_SERVER_ERROR("서버 에러입니다."),
    INVALID_DATE_INPUT("날짜 입력값이 올바르지 않습니다."),
    DATE_IS_TOO_FAR_IN_FUTURE("너무 미래의 날짜입니다");
    private final String description;
}
