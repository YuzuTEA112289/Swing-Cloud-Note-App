package com.menu.note.controller;

import com.menu.note.entity.Note;
import com.menu.note.repository.NoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;


@RestController
@RequestMapping("/api/notes")
@CrossOrigin(origins = "*") // 允许 Swing 跨域调用
public class NoteController {

    @Autowired
    private NoteRepository noteRepository;

    //保存笔记（失败时返回错误信息，成功时返回 Note 对象）
    @PostMapping("/save")

    public ResponseEntity<?> saveNote(@RequestBody Note note) {

        if (note.getTitle() == null || note.getTitle().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Title cannot be empty!");
        }
        if (note.getContent() == null || note.getContent().isBlank()) {
            return ResponseEntity.badRequest().body("Content cannot be empty!");
        }

        try {  //保存到数据库
            Note savedNote = noteRepository.save(note);
            return ResponseEntity.ok(savedNote);
        } catch (Exception e) {  //失败时返回错误信息
            System.err.println("Database saving exception: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal server error: Save failed due to database issues.");
        }
    }

    @GetMapping("/all")
    public List<Note> getAllNotes() {
        return noteRepository.findAll();
    }

    }

