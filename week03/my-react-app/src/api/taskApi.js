const BASE = "http://localhost:31888/api";

export async function fetchTasks(listId) {
  const res = await fetch(`${BASE}/lists/${listId}/tasks`);
  return res.json();
}

export async function createTask(listId, task) {
  const res = await fetch(`${BASE}/lists/${listId}/tasks`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(task),
  });
  return res.json();
}

export async function updateTask(task, listId) {
  const res = await fetch(`${BASE}/lists/${listId}/tasks/${task.id}`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(task),
  });
  return res.json();
}

export async function deleteTask(listId, taskId) {
  await fetch(`${BASE}/lists/${listId}/tasks/${taskId}`, { method: "DELETE" });
}

export async function resetTasks(listId) {
  await fetch(`${BASE}/lists/${listId}/tasks/reset`, { method: "POST" });
}
