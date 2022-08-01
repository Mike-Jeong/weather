package com.example.weather.repository;

import com.example.weather.domain.DateWeather;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DateWeatherRepository extends JpaRepository<DateWeather, LocalDate> {
    Optional<DateWeather> findByDate(LocalDate date);
    List<DateWeather> findAllByDate(LocalDate localDate);
}
