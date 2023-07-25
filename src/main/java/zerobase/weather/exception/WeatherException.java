package zerobase.weather.exception;

import lombok.*;
import zerobase.weather.type.ErrorCode;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WeatherException extends RuntimeException{
    private ErrorCode errorCode;
    private String errorMessage;

    public WeatherException(ErrorCode errorCode) {
        this.errorCode = errorCode;
        this.errorMessage = errorCode.getDescription();
    }
}
