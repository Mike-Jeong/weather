package com.example.weather.controller;

import com.example.weather.domain.Diary;
import com.example.weather.service.DiaryService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@RestController
public class DiaryController {

    private final DiaryService diaryService;

    @ApiOperation(value = "일기 텍스트와 날씨를 이용해서 DB에 일기 저장",
            notes = "일기를 작성하면 외부 API 에서 받아온 날씨 데이터와 함께 DB에 저장됩니다")
    @PostMapping("/create/diary")
    void createDiary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @ApiParam(value = "Created date", example = "2022-07-27") LocalDate date,
            @RequestBody String text
    ) {
        diaryService.createDiary(date, text);
    }

    @ApiOperation("선택한 날짜의 모든 일기 데이터를 가져옵니다")
    @GetMapping("/read/diary")
    List<Diary> readDiary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @ApiParam(value = "Date to look up", example = "2022-07-27") LocalDate date
    ) {
        return diaryService.readDiary(date);
    }

    @ApiOperation("Get all diaries from select period")
    @GetMapping("/read/diaries")
    List<Diary> readDiaries(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @ApiParam(value = "First date of Period to look up", example = "2022-07-01") LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @ApiParam(value = "Last date of Period to look up", example = "2022-07-27") LocalDate endDate
    ) {
        return diaryService.readDiaries(startDate, endDate);
    }

    @ApiOperation(value = "Modifying diary of selected date",
            notes = "If there has several diary on selected date, first one going to be modifying")
    @PutMapping("/update/diary")
    void updateDiary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @ApiParam(value = "Date to modify", example = "2022-07-27") LocalDate date,
            @RequestBody String text
    ) {
        diaryService.updateDiary(date, text);
    }

    @ApiOperation(value = "Delete diaries on selected date", notes = "All diaries will be deleted on selected date")
    @DeleteMapping("/delete/diary")
    void deleteDiary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @ApiParam(value = "삭제할 날짜", example = "2022-07-27") LocalDate date
    ) {
        diaryService.deleteDiary(date);
    }
}

