package com.menu.note.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;


public class Swing_Cloud_Note_App extends JFrame implements ActionListener {
    // 组件定义
    private JTextArea JTA = new JTextArea();
    private JScrollPane JSP = new JScrollPane(JTA);
    private JFileChooser JFC = new JFileChooser();

    // 存储从后端加载的笔记列表
    private List<NoteEntry> loadedNotes = new ArrayList<>();

    // 定义后端服务地址
    private static final String API_BASE_URL = "http://localhost:8080/api/notes";
    private final ObjectMapper objectMapper = new ObjectMapper();


    public Swing_Cloud_Note_App() {
        // 设置窗体基本属性
        setTitle("The Editor - Swing Cloud Version");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // 创建菜单栏
        JMenuBar jmb = new JMenuBar();

        // --- File Menu ---
        JMenu fileMenu = new JMenu("File");
        addMenuItem(fileMenu, "New");
        JMenu openSubMenu = new JMenu("Open");
        addMenuItem(openSubMenu, "From Local");
        addMenuItem(openSubMenu, "From Cloud");
        fileMenu.add(openSubMenu);
        JMenu saveAsSubMenu = new JMenu("Save As  ");
        addMenuItem(saveAsSubMenu, "Local File");
        addMenuItem(saveAsSubMenu, "Cloud File");
        fileMenu.add(saveAsSubMenu);
        fileMenu.addSeparator();
        addMenuItem(fileMenu, "Exit");

        // --- Edit Menu ---
        JMenu editMenu = new JMenu("Edit");
        addMenuItem(editMenu, "Cut");
        addMenuItem(editMenu, "Copy");
        addMenuItem(editMenu, "Paste");
        addMenuItem(editMenu, "Delete");

        // --- Configure Menu ---
        JMenu configMenu = new JMenu("Configure");
        JMenu colorMenu = new JMenu("Set Color  ");
        addMenuItem(colorMenu, "Set Background Color");
        addMenuItem(colorMenu, "Set Font Color");
        configMenu.add(colorMenu);

        JMenu fontMenu = new JMenu("Set Font  ");
        addMenuItem(fontMenu, "Set Font Size");
        configMenu.add(fontMenu);

        // --- Help Menu ---
        JMenu helpMenu = new JMenu("Help");
        addMenuItem(helpMenu, "About");

        // 组合菜单
        jmb.add(fileMenu);
        jmb.add(editMenu);
        jmb.add(configMenu);
        jmb.add(helpMenu);
        setJMenuBar(jmb);

        // 添加文本域
        JTA.setFont(new Font("Monospaced", Font.PLAIN, 16));
        JTA.setLineWrap(true);
        add(JSP);

        setVisible(true);
    }

    // 快速添加菜单项并注册监听
    private void addMenuItem(JMenu menu, String name) {
        JMenuItem item = new JMenuItem(name);
        item.addActionListener(this);
        menu.add(item);
    }

    // 处理菜单事件
    @Override
    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();

        // --- File 功能逻辑 ---
        if (cmd.equals("New")) {
            int s = JOptionPane.showConfirmDialog(this, "Are you sure to build new one?\n", "New", JOptionPane.YES_NO_OPTION);
            if (s == JOptionPane.YES_OPTION) JTA.setText("");
        }
        // 从本地打开文件
        else if (cmd.equals("From Local")) {
            if (JFC.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = JFC.getSelectedFile();
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    JTA.read(reader, null);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, "File read error!");
                }
            }
        }
        // 从云端数据库打开文件
        else if (cmd.equals("From Cloud")) {
            loadNotesFromCloud();
        }
        // 保存到本地
        else if (cmd.equals("Local File")) {
            if (JFC.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = JFC.getSelectedFile();
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                    JTA.write(writer);
                    JOptionPane.showMessageDialog(this, "Note saved to local successfully!!");
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, "Local save error!");
                }
            }
        }
        // 保存到云端数据库
        else if (cmd.equals("Cloud File")) {
            String title = JOptionPane.showInputDialog(this, "Enter note title:", "Save Note to Cloud", JOptionPane.QUESTION_MESSAGE);
            if (title != null && !title.trim().isEmpty()) {
                uploadToCloud(title, JTA.getText());
            }
        } else if (cmd.equals("Exit")) {
            if (JOptionPane.showConfirmDialog(this, "Exit Program?", "Exit", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
                System.exit(0);
        }

        // --- Edit 功能逻辑 ---
        else if (cmd.equals("Cut")) {
            JTA.cut();
        } else if (cmd.equals("Copy")) {
            JTA.copy();
        } else if (cmd.equals("Paste")) {
            JTA.paste();
        } else if (cmd.equals("Delete")) {
            JTA.replaceSelection("");
        }

        // --- Configure 功能逻辑  ---
        else if (cmd.equals("Set Background Color")) {
            Color c = JColorChooser.showDialog(this, "Set Background Color", JTA.getBackground());
            if (c != null) JTA.setBackground(c);
        } else if (cmd.equals("Set Font Color")) {
            Color c = JColorChooser.showDialog(this, "Set Font Color", JTA.getForeground());
            if (c != null) JTA.setForeground(c);
        } else if (cmd.equals("Set Font Size")) {
            String sizeStr = JOptionPane.showInputDialog(this, "Enter font size (e.g. 20):", "Font Size", JOptionPane.QUESTION_MESSAGE);
            if (sizeStr != null) {
                try {
                    int size = Integer.parseInt(sizeStr);
                    JTA.setFont(new Font(JTA.getFont().getFamily(), Font.PLAIN, size));
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Please enter a valid number.");
                }
            }
        }

        // --- Help 功能逻辑 ---
        else if (cmd.equals("About")) {
            JOptionPane.showMessageDialog(this,
                    "Project: Welcome to Swing Cloud Note!\n",
                    "About", JOptionPane.INFORMATION_MESSAGE);
        }
    }


    private void uploadToCloud(String title, String content) {
        // 创建一个用于发送的 NoteRequest 对象
        NoteRequest noteToSend = new NoteRequest(title, content);

        String json = "";
        try {
            // 将对象转换为 JSON 字符串(Jackson自动处理转义)
            json = objectMapper.writeValueAsString(noteToSend);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            JOptionPane.showMessageDialog(this, "Failed to create JSON for note: " + e.getMessage(), "JSON Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/save"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json, java.nio.charset.StandardCharsets.UTF_8))
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200 || response.statusCode() == 201) {
                JOptionPane.showMessageDialog(this, "Note saved to cloud successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Failed to save note. HTTP Status: " + response.statusCode() + "\nBackend Response: " + response.body(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException | InterruptedException ex) {
            JOptionPane.showMessageDialog(this, "Connection error: " + ex.getMessage() +
                    "\nPlease ensure Spring Boot backend is running at " + API_BASE_URL + "/save!", "Connection Error", JOptionPane.ERROR_MESSAGE);
            System.err.println("Error uploading note: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // 从云端加载所有笔记，并显示选择对话框
    private void loadNotesFromCloud() {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/all"))
                .GET()
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenAccept(response -> {
                    SwingUtilities.invokeLater(() -> { // 确保在 AWT 事件调度线程中更新 UI
                        if (response.statusCode() == 200) {
                            String responseBody = response.body();
                            System.out.println("Loaded from cloud: " + responseBody);

                            // 清空旧的笔记列表
                            loadedNotes.clear();

                            // 使用 Jackson 解析 JSON 数组
                            try {
                                NoteEntry[] notesArray = objectMapper.readValue(responseBody, NoteEntry[].class);

                                if (notesArray.length == 0) {
                                    JOptionPane.showMessageDialog(this, "No notes found in the cloud.", "Information", JOptionPane.INFORMATION_MESSAGE);
                                    JTA.setText(""); // 清空文本区域
                                    return;
                                }
                                for (NoteEntry note : notesArray) {
                                    loadedNotes.add(note);
                                }

                                // 创建一个 JList 用于显示笔记标题
                                String[] titles = loadedNotes.stream().map(NoteEntry::getTitle).toArray(String[]::new);
                                JList<String> noteList = new JList<>(titles);
                                JScrollPane scrollPane = new JScrollPane(noteList);
                                noteList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // 单选

                                int result = JOptionPane.showConfirmDialog(this, scrollPane,
                                        "Select a Note to Load", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

                                if (result == JOptionPane.OK_OPTION && noteList.getSelectedIndex() != -1) {
                                    NoteEntry selectedNote = loadedNotes.get(noteList.getSelectedIndex());
                                    JTA.setText(selectedNote.getContent());
                                } else if (result == JOptionPane.CANCEL_OPTION) {
                                } // 若用户取消选择，不做任何操作

                            } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                                JOptionPane.showMessageDialog(this, "Error parsing notes from cloud: " + e.getMessage(), "JSON Parse Error", JOptionPane.ERROR_MESSAGE);
                                System.err.println("Error parsing notes from cloud: " + e.getMessage());
                                e.printStackTrace();
                            }

                        } else {
                            JOptionPane.showMessageDialog(this,
                                    "Failed to load notes. HTTP Status: " + response.statusCode() + "\nBackend Response: " + response.body(),
                                    "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    });
                })
                .exceptionally(ex -> {
                    SwingUtilities.invokeLater(() ->
                            JOptionPane.showMessageDialog(this, "Connection error: " + ex.getMessage() +
                                    "\nPlease ensure Spring Boot backend is running at " + API_BASE_URL + "/all!", "Connection Error", JOptionPane.ERROR_MESSAGE));
                    System.err.println("Error loading notes: " + ex.getMessage());
                    ex.printStackTrace();
                    return null;
                });
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
        }
        new Swing_Cloud_Note_App();
    }


    // 用于向后端发送笔记请求
    private static class NoteRequest {
        public String title;
        public String content;

        public NoteRequest(String title, String content) {
            this.title = title;
            this.content = content;
        }

    }

    // 用于存储从后端加载的笔记的内部类
    private static class NoteEntry {
        private Long id;
        private String title;
        private String content;

        public NoteEntry() {} // Jackson 需要无参构造函数

        public void setId(Long id) {this.id = id;}
        public Long getId() {return id;}


        public void setTitle(String title) { this.title = title; }
        public String getTitle() { return title; }

        public void setContent(String content) { this.content = content; }
        public String getContent() { return content;

        }
    }
}
