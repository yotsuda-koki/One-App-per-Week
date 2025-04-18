package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.model.TaskList;

public interface TaskListRepository extends JpaRepository<TaskList, Long> {
}