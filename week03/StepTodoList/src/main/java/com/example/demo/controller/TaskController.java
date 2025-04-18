package com.example.demo.controller;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.Task;
import com.example.demo.model.TaskList;
import com.example.demo.repository.TaskListRepository;
import com.example.demo.repository.TaskRepository;

@RestController
@RequestMapping("/api/lists/{listId}/tasks")
@CrossOrigin(origins = "*")
public class TaskController {

	private final TaskRepository taskRepo;
	private final TaskListRepository listRepo;

	public TaskController(TaskRepository taskRepo, TaskListRepository listRepo) {
		this.taskRepo = taskRepo;
		this.listRepo = listRepo;
	}

	@GetMapping
	public List<Task> getTasks(@PathVariable Long listId) {
		return taskRepo.findByTaskListId(listId);
	}

	@PostMapping
	public Task addTask(@PathVariable Long listId, @RequestBody Task task) {
		TaskList list = listRepo.findById(listId).orElseThrow();
		task.setTaskList(list);
		return taskRepo.save(task);
	}

	@PutMapping("/{id}")
	public Task updateTask(@PathVariable Long id, @RequestBody Task updatedTask) {
		return taskRepo.findById(id).map(task -> {
			task.setTitle(updatedTask.getTitle());
			task.setDescription(updatedTask.getDescription());
			task.setDueDate(updatedTask.getDueDate());
			task.setCompleted(updatedTask.isCompleted());
			return taskRepo.save(task);
		}).orElseThrow();
	}

	@DeleteMapping("/{id}")
	public void deleteTask(@PathVariable Long id) {
		taskRepo.deleteById(id);
	}

	@PostMapping("/reset")
	public void resetTasks(@PathVariable Long listId) {
		List<Task> tasks = taskRepo.findByTaskListId(listId);
		for (Task task : tasks) {
			task.setCompleted(false);
			task.setDueDate(null);
			taskRepo.save(task);
		}
	}
}
