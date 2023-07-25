package zerobase.weather.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    WEATHER_DATA_PARSING_ERROR("날씨 데이터 파싱이 되지 않습니다."),
    CANNOT_GET_WEATHER_DATA_FROM_API("날씨 데이터를 api로부터 가져오지 못했습니다."),
    AMOUNT_IS_TOO_BIG("거래 금액이 너무 큽니다");
    private final String description;
}
