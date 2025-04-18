const BASE = "http://localhost:31888/api";

export async function fetchLists() {
  const res = await fetch(`${BASE}/lists`);
  return res.json();
}

export async function createList(name) {
  const res = await fetch(`${BASE}/lists`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ name }),
  });
  return res.json();
}

export async function deleteList(listId) {
  await fetch(`${BASE}/lists/${listId}`, { method: "DELETE" });
}
