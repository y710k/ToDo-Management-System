package com.dmm.task.controller;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.dmm.task.data.entity.Tasks;
import com.dmm.task.data.repository.TasksRepository;
import com.dmm.task.form.TaskForm;
import com.dmm.task.service.AccountUserDetails;

@Controller
public class MainController {
	@Autowired
	private TasksRepository repo;
	
	//カレンダー表示
	@GetMapping("/main")
	public String main(Model model) {

		// ①週と日を格納する二次元配列を用意する
		List<List<LocalDate>> month = new ArrayList<>();

		// ②1週間分のLocalDateを格納するListを用意する
		List<LocalDate> sevenDays = new ArrayList<>();

		// 日にちを格納する変数"day"を用意して現在日時の取得
		LocalDate day = LocalDate.now();

		// 現在日時からその年月のついたちを取得
		day = LocalDate.of(day.getYear(), day.getMonthValue(), 1);

		// 当月表示
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy年MM月");
		model.addAttribute("month", day.format(dateTimeFormatter));
		// 前月、翌月表示
		model.addAttribute("prev", day.minusMonths(1));
		model.addAttribute("next", day.plusMonths(1));

		// 前月分のLocalDateを求める
		DayOfWeek week = day.getDayOfWeek(); // ついたちの曜日を取得

		day = day.minusDays(week.getValue()); // 初週の日曜日の日の値
		/*
		 * week.getValue()でついたちの曜日の値[月(1)～日(7)]を取得(今回は11/1は金曜日なので5)
		 * day.minusDays(5);となる。5日戻った日の値をdayに代入(day = 27)
		 */

		for (int i = 1; i <= 7; i++) { // 1日ずつ増やして１週間分LocalDateを求める
			sevenDays.add(day); // ②で作成したListへ格納する
			day = day.plusDays(1);
		}
		month.add(sevenDays); // 1週間分詰めたら①のリストへ格納する

		// 2週目以降のリストを新しく生成
		sevenDays = new ArrayList<>();

		for (int i = 7; i <= day.lengthOfMonth(); i++) {
			sevenDays.add(day);
			week = day.getDayOfWeek();

			if (week == DayOfWeek.SATURDAY) {
				month.add(sevenDays);
				sevenDays = new ArrayList<>();
				day = day.plusDays(1);
			} else {
				day = day.plusDays(1);
			}
		}
		sevenDays.clear(); // 残っているリストの中身削除

		day = LocalDate.now(); // 現在日時を再取得
		// 当月の最終日を取得
		day = LocalDate.of(day.getYear(), day.getMonthValue(), day.lengthOfMonth());

		DayOfWeek lastWeek = day.getDayOfWeek(); // 最終日の曜日を取得(今回は11/30は土曜日(6))
		day = day.minusDays(lastWeek.getValue());// 最終日の6日前の日曜日11/24
		int lastDay = day.getDayOfMonth() + 6; // 最終週の日曜日+6日

		for (int i = day.getDayOfMonth(); i <= lastDay; i++) { // 最終週
			sevenDays.add(day);
			day = day.plusDays(1);
		}
		month.add(sevenDays);
		model.addAttribute("matrix", month);

		// 日付とタスクを紐付けるコレクション（ひとまず空のままでOK）
		MultiValueMap<LocalDate, Tasks> tasks = new LinkedMultiValueMap<LocalDate, Tasks>();
		model.addAttribute("tasks", tasks);

		return "main";

	}
	//タスク登録ページの表示
	@GetMapping("/main/create/{date}")
	public String create(Model model,@PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd")LocalDate date) {
		return "create";
	}
	
	//タスク新規登録
	@PostMapping("/main/create")
	public String createPost(Model model, TaskForm tasks, @AuthenticationPrincipal AccountUserDetails user) {
		
		Tasks task = new Tasks();
		task.setName(user.getName());
		task.setTitle(tasks.getTitle());
		task.setText(tasks.getText());
		task.setDate(tasks.getDate());
		task.setDone(tasks.isDone());
		repo.save(task);
		
		return "redirect:/main";
	}
	
	/*
	//タスク表示機能
	@GetMapping("/main")
	public String taskView() {
		return "main";
	}
	
	//タスク編集・削除
	@PostMapping("/main/edit/{id}(id=${task.id})")
	public String edit() {
		return "redirect:/main";
	}
	*/
	

}
