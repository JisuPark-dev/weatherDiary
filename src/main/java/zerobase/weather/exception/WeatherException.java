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
        super("errorCode : " +errorCode.toString()+", errormessage : "+errorCode.getDescription());
        this.errorCode = errorCode;
        this.errorMessage = errorCode.getDescription();
    }

    @Override
    public String toString() {
        return "WeatherException{" +
                "errorCode=" + errorCode +
                ", errorMessage='" + errorMessage + '\'' +
                '}';
    }


}
