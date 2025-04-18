import { useEffect, useState } from "react";
import {
  Box,
  Card,
  CardContent,
  Typography,
  Button,
  TextField,
  Stack,
  MenuItem,
  Select,
  InputLabel,
  FormControl,
  createTheme,
  ThemeProvider,
  Dialog,
} from "@mui/material";
import { fetchLists, createList, deleteList } from "../api/listApi";
import {
  fetchTasks,
  createTask,
  updateTask,
  deleteTask,
  resetTasks,
} from "../api/taskApi";

const darkTheme = createTheme({
  palette: {
    mode: "dark",
    background: { default: "#1e1e1e", paper: "#2c2c2c" },
    primary: { main: "#90caf9" },
    secondary: { main: "#f48fb1" },
  },
});

export default function StepTodoList() {
  const [lists, setLists] = useState([]);
  const [selectedListId, setSelectedListId] = useState("");
  const [tasks, setTasks] = useState([]);
  const [currentIndex, setCurrentIndex] = useState(0);
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [newListName, setNewListName] = useState("");
  const [isEditing, setIsEditing] = useState(false);
  const [editTitle, setEditTitle] = useState("");
  const [editDescription, setEditDescription] = useState("");
  const [editDueDate, setEditDueDate] = useState("");
  const [showDialog, setShowDialog] = useState(false);
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);
  const [showTaskDeleteConfirm, setShowTaskDeleteConfirm] = useState(false);
  const [showCompleteConfirm, setShowCompleteConfirm] = useState(false);

  const currentTask = tasks[currentIndex];

  useEffect(() => {
    fetchLists().then((data) => {
      setLists(data);
      if (data.length > 0) setSelectedListId(data[0].id);
    });
  }, []);

  useEffect(() => {
    if (selectedListId) {
      fetchTasks(selectedListId).then(setTasks);
      setCurrentIndex(0);
    }
  }, [selectedListId]);

  const handleAddTask = async () => {
    if (!title.trim() || !selectedListId) return;
    const newTask = await createTask(selectedListId, {
      title: title.trim(),
      description: description.trim(),
      completed: false,
      dueDate: null,
    });
    const updated = await fetchTasks(selectedListId);
    setTasks(updated);
    setTitle("");
    setDescription("");
    setCurrentIndex(updated.length - 1);
  };

  const handleComplete = async () => {
    if (!currentTask) return;
    await updateTask({ ...currentTask, completed: true }, selectedListId);
    if (currentIndex < tasks.length - 1) {
      setCurrentIndex(currentIndex + 1);
    } else {
      await resetTasks(selectedListId);
      const reset = await fetchTasks(selectedListId);
      setTasks(reset);
      setCurrentIndex(0);
      setShowDialog(true);
    }
  };

  const handleBack = async () => {
    if (currentIndex > 0) {
      const prevIndex = currentIndex - 1;
      const updated = { ...tasks[prevIndex], completed: false };
      await updateTask(updated, selectedListId);
      const refreshed = await fetchTasks(selectedListId);
      setTasks(refreshed);
      setCurrentIndex(prevIndex);
    }
  };

  const handleDueDateChange = async (e) => {
    const updated = { ...currentTask, dueDate: e.target.value };
    await updateTask(updated, selectedListId);
    const newTasks = await fetchTasks(selectedListId);
    setTasks(newTasks);
  };

  const handleDelete = async () => {
    await deleteTask(selectedListId, currentTask.id);
    const updated = await fetchTasks(selectedListId);
    setTasks(updated);
    setCurrentIndex((prev) => Math.min(prev, updated.length - 1));
  };

  const handleCreateList = async () => {
    if (!newListName.trim()) return;
    const newList = await createList(newListName);
    const updatedLists = await fetchLists();
    setLists(updatedLists);
    setSelectedListId(newList.id);
    setNewListName("");
  };

  const handleStartEdit = () => {
    setEditTitle(currentTask.title);
    setEditDescription(currentTask.description);
    setEditDueDate(currentTask.dueDate || "");
    setIsEditing(true);
  };

  const handleSaveEdit = async () => {
    const updated = {
      ...currentTask,
      title: editTitle,
      description: editDescription,
      dueDate: editDueDate,
    };
    await updateTask(updated, selectedListId);
    const refreshed = await fetchTasks(selectedListId);
    setTasks(refreshed);
    setIsEditing(false);
  };

  const handleDeleteList = async () => {
    if (!selectedListId) return;
    await deleteList(selectedListId);
    const updatedLists = await fetchLists();
    setLists(updatedLists);
    setSelectedListId(updatedLists.length > 0 ? updatedLists[0].id : "");
    setTasks([]);
    setCurrentIndex(0);
  };

  const handleCancelEdit = () => {
    setIsEditing(false);
  };

  const completedCount = tasks.filter((t) => t.completed).length;

  return (
    <ThemeProvider theme={darkTheme}>
      <Box
        sx={{
          bgcolor: "background.default",
          color: "white",
          minHeight: "100vh",
        }}
      >
        <Box sx={{ maxWidth: 700, mx: "auto" }}>
          <Typography variant="h4" align="center" gutterBottom>
            ステップToDoリスト
          </Typography>

          {/* リスト選択と作成 */}
          <Stack direction="row" spacing={2} alignItems="center" sx={{ mb: 3 }}>
            <FormControl fullWidth>
              <InputLabel sx={{ color: "white" }}>リスト選択</InputLabel>
              <Select
                value={selectedListId}
                label="リスト選択"
                onChange={(e) => setSelectedListId(e.target.value)}
                sx={{ color: "white", bgcolor: "background.paper" }}
              >
                {lists.map((list) => (
                  <MenuItem key={list.id} value={list.id}>
                    {list.name}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>

            <TextField
              label="新しいリスト名"
              value={newListName}
              onChange={(e) => setNewListName(e.target.value)}
              size="small"
              sx={{ input: { color: "white" } }}
            />
            <Button onClick={handleCreateList} variant="contained">
              作成
            </Button>
            <Button
              onClick={() => setShowDeleteConfirm(true)}
              variant="outlined"
              color="error"
            >
              削除
            </Button>
          </Stack>

          {/* タスク追加 */}
          <Card sx={{ width: "100%", mb: 4 }}>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                タスク追加
              </Typography>
              <Stack spacing={2}>
                <TextField
                  fullWidth
                  label="タイトル"
                  variant="filled"
                  value={title}
                  onChange={(e) => setTitle(e.target.value)}
                  sx={{
                    bgcolor: "background.paper",
                    input: { color: "white" },
                  }}
                />
                <TextField
                  fullWidth
                  label="詳細"
                  variant="filled"
                  value={description}
                  onChange={(e) => setDescription(e.target.value)}
                  multiline
                  minRows={4}
                  sx={{
                    bgcolor: "background.paper",
                    textarea: { color: "white" },
                  }}
                />
                <Box display="flex" justifyContent="flex-end">
                  <Button variant="contained" onClick={handleAddTask}>
                    追加
                  </Button>
                </Box>
              </Stack>
            </CardContent>
          </Card>

          {/* 現在のタスク表示 */}
          {currentTask && (
            <Card
              sx={{
                width: "100%",
                mb: 4,
              }}
            >
              <CardContent>
                <Box
                  sx={{
                    display: "flex",
                    justifyContent: "space-between",
                    alignItems: "center",
                    mb: 2,
                  }}
                >
                  {isEditing ? (
                    <TextField
                      fullWidth
                      label="タイトルを編集"
                      value={editTitle}
                      onChange={(e) => setEditTitle(e.target.value)}
                      sx={{ input: { color: "white" }, flex: 1, mr: 2 }}
                    />
                  ) : (
                    <Typography
                      variant="h5"
                      sx={{ fontWeight: "bold", color: "primary.light" }}
                    >
                      📝 {currentTask.title}
                    </Typography>
                  )}
                  <Stack direction="row" spacing={1}>
                    {isEditing ? (
                      <>
                        <Button
                          variant="contained"
                          color="success"
                          onClick={handleSaveEdit}
                        >
                          保存
                        </Button>
                        <Button variant="outlined" onClick={handleCancelEdit}>
                          キャンセル
                        </Button>
                      </>
                    ) : (
                      <>
                        <Button variant="outlined" onClick={handleStartEdit}>
                          編集 ✏️
                        </Button>
                        <Button
                          variant="outlined"
                          color="error"
                          onClick={() => setShowTaskDeleteConfirm(true)}
                        >
                          削除 🗑
                        </Button>
                      </>
                    )}
                  </Stack>
                </Box>

                {isEditing ? (
                  <TextField
                    fullWidth
                    label="詳細を編集"
                    multiline
                    minRows={4}
                    value={editDescription}
                    onChange={(e) => setEditDescription(e.target.value)}
                    sx={{
                      mb: 3,
                      bgcolor: "background.paper",
                      textarea: { color: "white" },
                    }}
                  />
                ) : (
                  <Typography
                    variant="body1"
                    sx={{
                      mb: 3,
                      whiteSpace: "pre-wrap",
                      lineHeight: 1.8,
                      minHeight: "6em",
                      backgroundColor: "#1f1f1f",
                      p: 2,
                      borderRadius: 1,
                    }}
                  >
                    {currentTask.description || "（詳細は入力されていません）"}
                  </Typography>
                )}

                {isEditing ? (
                  <TextField
                    label="期限を編集"
                    type="datetime-local"
                    value={editDueDate}
                    onChange={(e) => setEditDueDate(e.target.value)}
                    InputLabelProps={{ shrink: true }}
                    fullWidth
                    sx={{
                      mb: 3,
                      bgcolor: "background.paper",
                      input: { color: "white" },
                    }}
                  />
                ) : (
                  <>
                    <TextField
                      label="このステップの期限"
                      type="datetime-local"
                      value={currentTask.dueDate || ""}
                      onChange={handleDueDateChange}
                      InputLabelProps={{ shrink: true }}
                      fullWidth
                      sx={{
                        mb: 3,
                        bgcolor: "background.paper",
                        input: { color: "white" },
                      }}
                    />
                    {currentTask.dueDate && (
                      <Typography
                        variant="body2"
                        sx={{ color: "warning.main", mb: 2 }}
                      >
                        締切: {new Date(currentTask.dueDate).toLocaleString()}
                      </Typography>
                    )}
                  </>
                )}

                <Box
                  sx={{
                    display: "flex",
                    justifyContent: "space-between",
                    alignItems: "center",
                  }}
                >
                  <Button
                    variant="outlined"
                    onClick={handleBack}
                    disabled={currentIndex === 0}
                  >
                    ← 戻る
                  </Button>
                  <Button
                    variant="contained"
                    onClick={() => {
                      if (currentIndex === tasks.length - 1) {
                        setShowCompleteConfirm(true);
                      } else {
                        handleComplete();
                      }
                    }}
                  >
                    {currentIndex === tasks.length - 1 ? "完了" : "次へ →"}
                  </Button>
                </Box>
              </CardContent>
            </Card>
          )}

          <Dialog open={showDialog} onClose={() => setShowDialog(false)}>
            <Card sx={{ p: 2, minWidth: 300 }}>
              <Typography variant="h6" gutterBottom>
                お疲れさまでした！
              </Typography>
              <Typography variant="body1" gutterBottom>
                すべてのタスクが完了しました!
              </Typography>
              <Box textAlign="right">
                <Button
                  onClick={() => setShowDialog(false)}
                  variant="contained"
                >
                  OK
                </Button>
              </Box>
            </Card>
          </Dialog>

          <Dialog
            open={showDeleteConfirm}
            onClose={() => setShowDeleteConfirm(false)}
          >
            <Card sx={{ p: 2, minWidth: 300 }}>
              <Typography variant="h6" gutterBottom>
                本当に削除しますか？
              </Typography>
              <Typography variant="body2" gutterBottom>
                リストと関連するタスクはすべて削除されます。
              </Typography>
              <Box textAlign="right" mt={2}>
                <Button
                  onClick={async () => {
                    await handleDeleteList();
                    setShowDeleteConfirm(false);
                  }}
                  variant="contained"
                  color="error"
                >
                  削除する
                </Button>
                <Button
                  onClick={() => setShowDeleteConfirm(false)}
                  sx={{ ml: 1 }}
                  variant="outlined"
                >
                  キャンセル
                </Button>
              </Box>
            </Card>
          </Dialog>

          <Dialog
            open={showTaskDeleteConfirm}
            onClose={() => setShowTaskDeleteConfirm(false)}
          >
            <Card sx={{ p: 2, minWidth: 300 }}>
              <Typography variant="h6" gutterBottom>
                このタスクを削除しますか？
              </Typography>
              <Typography variant="body2" gutterBottom>
                元に戻せません。
              </Typography>
              <Box textAlign="right" mt={2}>
                <Button
                  onClick={async () => {
                    await handleDelete();
                    setShowTaskDeleteConfirm(false);
                  }}
                  variant="contained"
                  color="error"
                >
                  削除する
                </Button>
                <Button
                  onClick={() => setShowTaskDeleteConfirm(false)}
                  variant="outlined"
                  sx={{ ml: 1 }}
                >
                  キャンセル
                </Button>
              </Box>
            </Card>
          </Dialog>

          <Dialog
            open={showCompleteConfirm}
            onClose={() => setShowCompleteConfirm(false)}
          >
            <Card sx={{ p: 2, minWidth: 300 }}>
              <Typography variant="h6" gutterBottom>
                全タスク完了の確認
              </Typography>
              <Typography variant="body1" gutterBottom>
                すべてのタスクを完了としてマークしますか？
              </Typography>
              <Box textAlign="right">
                <Button
                  variant="contained"
                  onClick={async () => {
                    setShowCompleteConfirm(false);
                    await handleComplete();
                  }}
                >
                  完了する
                </Button>
                <Button
                  variant="outlined"
                  onClick={() => setShowCompleteConfirm(false)}
                  sx={{ ml: 1 }}
                >
                  キャンセル
                </Button>
              </Box>
            </Card>
          </Dialog>
        </Box>
      </Box>
    </ThemeProvider>
  );
}
