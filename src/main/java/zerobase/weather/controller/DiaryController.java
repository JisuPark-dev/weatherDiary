package zerobase.weather.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import zerobase.weather.domain.Diary;
import zerobase.weather.service.DiaryService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class DiaryController {
    private final DiaryService diaryService;


    @ApiOperation(value = "일기 텍스트와 날씨를 이용해서 DB에 일기 저장" , notes = "2050년 이상 연도 입력시, 오류처리")
    @PostMapping("/create/diary")
    void createDiary(
            @RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE)
            @ApiParam(value = "일기 작성 날짜", example = "2020-02-20")LocalDate date,
            @RequestBody @ApiParam(value="작성할 일기 본문") String text
    ) {
        diaryService.createDiary(date, text);
    }

    @ApiOperation("하루의 모든 일기 조회")
    @GetMapping("/read/diary")
    List<Diary> readDiary(
            @RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE)
            @ApiParam(value = "조회할 날짜", example = "2020-02-20")LocalDate date
    ) {
        return diaryService.readDiary(date);
    }

    @ApiOperation("일정 기간안의 모든 일기 조회")
    @GetMapping("/read/diaries")
    List<Diary> readDiaries(
            @RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE)
            @ApiParam(value = "조회할 기간의 시작 날짜", example = "2020-02-20")LocalDate startDate,
            @RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE)
            @ApiParam(value = "조회할 기간의 마지막 날짜", example = "2020-02-20")LocalDate endDate
    ) {
        return diaryService.readDiaries(startDate, endDate);
    }

    @ApiOperation("특정 날짜의 첫번째 일기 수정")
    @PutMapping("/update/diary")
    void updateDiary(
            @RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE)
            @ApiParam(value = "수정하고자 하는 일기의 날짜", example = "2020-02-20")LocalDate date,
            @RequestBody @ApiParam(value="수정할 일기 본문") String text
    ) {
        diaryService.updateDiary(date, text);
    }

    @ApiOperation("특정 날짜의 모든 일기 삭제")
    @DeleteMapping("/delete/diary")
    void deleteDiary(
            @RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE)
            @ApiParam(value = "삭제 하고자 하는 일기의 날짜", example = "2020-02-20")LocalDate date
    ) {
        diaryService.deleteDiary(date);
    }

}
