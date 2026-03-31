package com.menu.note.entity;

import jakarta.persistence.*;
import lombok.Data; // 使用 Lombok 简化 Getter/Setter


@Entity
@Table(name = "cloud_note") // 新建数据库表名
@Data // Lombok 注解（自动生成 getter, setter, toString 等）
public class Note {

    @Id // 主键
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 自增 ID
    private Long id;

    @Column(nullable = false, length = 255) // 保证标题不能为空，限定最大长度
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT") // 保证内容不能为空，并声明为长文本
    private String content;

    public Note() {} // JPA 需要无参构造函数

}
