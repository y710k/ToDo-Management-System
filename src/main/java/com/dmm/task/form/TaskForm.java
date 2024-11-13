package com.dmm.task.form;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.Data;

@Data
public class TaskForm {
	private String title;
	private String text;
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDateTime date;
	private boolean done;
}
