package com.example.demo.controller;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.TaskList;
import com.example.demo.repository.TaskListRepository;

@RestController
@RequestMapping("/api/lists")
@CrossOrigin(origins = "*")
public class TaskListController {

	private final TaskListRepository taskListRepo;

	public TaskListController(TaskListRepository taskListRepo) {
		this.taskListRepo = taskListRepo;
	}

	@GetMapping
	public List<TaskList> getAllLists() {
		return taskListRepo.findAll();
	}

	@PostMapping
	public TaskList createList(@RequestBody TaskList list) {
		return taskListRepo.save(list);
	}

	@DeleteMapping("/{id}")
	public void deleteList(@PathVariable Long id) {
		taskListRepo.deleteById(id);
	}
}
