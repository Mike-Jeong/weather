package com.example.weather.service;

import com.example.weather.WeatherApplication;
import com.example.weather.domain.DateWeather;
import com.example.weather.domain.Diary;
import com.example.weather.repository.DateWeatherRepository;
import com.example.weather.repository.DiaryRepository;
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
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class DiaryService {

    @Value("${openWeather.apikey}")
    private String apiKey;

    private final DiaryRepository diaryRepository;
    private final DateWeatherRepository dateWeatherRepository;

    private static final Logger logger = LoggerFactory.getLogger(WeatherApplication.class);

    @Transactional
    @Scheduled(cron = "0 0 1 * * *")
    public void saveWeatherDate() {

        dateWeatherRepository.save(getWeatherFromApi());
        logger.info("Save Weather Date done without error");
    }

    private DateWeather getWeatherFromApi() {

        String weatherData = getWeatherString();
        Map<String, Object> parsedWeather = parseWeather(weatherData);

        return DateWeather.builder()
                .date(LocalDate.now())
                .weather(parsedWeather.get("main").toString())
                .icon(parsedWeather.get("icon").toString())
                .temperature((Double) parsedWeather.get("temp"))
                .build();
    }

    private String getWeatherString() {

        String apiUrl = "https://api.openweathermap.org/data/2.5/weather?q=seoul&appid=" + apiKey;

        try {
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();
            BufferedReader br;
            if (responseCode == 200) {
                br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            } else {
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
            return "failed to get response";
        }
    }

    private Map<String, Object> parseWeather(String jsonString) {

        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject;

        try {
            jsonObject = (JSONObject) jsonParser.parse(jsonString);
        } catch (ParseException e) {
            throw new RuntimeException(e);
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

    @Transactional
    public void createDiary(LocalDate date, String text) {

        logger.info("stared to create dairy");
        Optional<DateWeather> dateWeatherOpt = dateWeatherRepository.findByDate(date);

        if (dateWeatherOpt.isPresent()) {

            DateWeather dateWeather = dateWeatherOpt.get();

            diaryRepository.save(Diary.builder()
                    .weather(dateWeather.getWeather())
                    .icon(dateWeather.getIcon())
                    .temperature(dateWeather.getTemperature())
                    .date(dateWeather.getDate())
                    .text(text)
                    .build()
            );
            logger.info("end to create diary");
        } else {
            logger.info("end to create diary with error");
        }
    }

    private DateWeather getDateWeather(LocalDate date) {

        List<DateWeather> dateWeatherListFromDB =
                dateWeatherRepository.findAllByDate(date);
        if (dateWeatherListFromDB.size() == 0) {
            return getWeatherFromApi();
        } else {
            return dateWeatherListFromDB.get(0);
        }
    }

    public List<Diary> readDiary(LocalDate date) {

        logger.debug("read diary");
        /*if (date.isAfter(LocalDate.ofYearDay(3050, 1))) {
            throw new InvalidDate();
        }*/
        return diaryRepository.findAllByDate(date);
    }

    public List<Diary> readDiaries(LocalDate startDate, LocalDate endDate) {

        return diaryRepository.findAllByDateBetween(startDate, endDate);
    }

    @Transactional
    public void updateDiary(LocalDate date, String text) {

        Optional<Diary> diaryOpt = diaryRepository.findFirstByDate(date);

        if (diaryOpt.isPresent()) {
            Diary nowDiary = diaryOpt.get();
            nowDiary.setText(text);
            diaryRepository.save(nowDiary);
        }
    }

    @Transactional
    public void deleteDiary(LocalDate date) {
        diaryRepository.deleteAllByDate(date);
    }
}
