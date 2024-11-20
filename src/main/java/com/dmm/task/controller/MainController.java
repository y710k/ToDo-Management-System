package com.dmm.task.controller;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.GrantedAuthority;
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

	// カレンダー表示
	@GetMapping("/main")
	public String main(Model model, @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
			@AuthenticationPrincipal AccountUserDetails user) {

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

		// 今月or前月or翌月を判定
		if (date == null) {
			// 今月と判断し当月の１日を取得する
			// 現在日時からその年月のついたちを取得
			day = LocalDate.of(day.getYear(), day.getMonthValue(), 1);
			model.addAttribute("month", day.format(dateTimeFormatter));

		} else {
			// dateに値が渡ってきたので前月 or 翌月と判断し、引数で受け取った日付をそのまま1日として使う
			day = LocalDate.of(date.getYear(), date.getMonthValue(), 1);
			model.addAttribute("month", day.format(dateTimeFormatter));
		}
		// 前月表示
		model.addAttribute("prev", day.minusMonths(1));
		// 翌月表示
		model.addAttribute("next", day.plusMonths(1));

		// 前月分のLocalDateを求める
		DayOfWeek week = day.getDayOfWeek(); // ついたちの曜日を取得
		if (week == DayOfWeek.SUNDAY) {
			// 日曜日なら何もしない
		} else {
			// 日曜日以外の処理
			day = day.minusDays(week.getValue()); // 初週の日曜日の日の値
			/*
			 * week.getValue()でついたちの曜日の値[月(1)～日(7)]を取得(今回は11/1は金曜日なので5)
			 * day.minusDays(5);となる。5日戻った日の値をdayに代入(day = 27)
			 */
		}
		LocalDate firstDay = day; // カレンダーの最初の日
		for (int i = 1; i <= 7; i++) { // 1日ずつ増やして１週間分LocalDateを求める
			sevenDays.add(day); // ②で作成したListへ格納する
			day = day.plusDays(1);
		}
		month.add(sevenDays); // 1週間分詰めたら①のリストへ格納する

		// 2週目以降のリストを新しく生成
		sevenDays = new ArrayList<>();

		for (int i = 7; i <= day.lengthOfMonth(); i++) { //7～月の最後の値まで繰り返す
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
		sevenDays.clear(); // 中途半端に残っているリストの中身削除
		
		// 最終週
		// 当月の最終日を取得
		DayOfWeek lastWeek = day.getDayOfWeek(); // 最終日の曜日を取得(今回は11/30は土曜日(6))
		day = day.minusDays(lastWeek.getValue());// 最終週の日曜日
		LocalDate lastSaturday = day.plusDays(6);// 最終日の土曜日
		int lastDay = day.getDayOfMonth() + 6;

		// カレンダー最終日
		LocalDate endDay = lastSaturday;
		for (int i = day.getDayOfMonth(); i <= lastDay; i++) { // 最終週

			sevenDays.add(day);
			day = day.plusDays(1);
		}

		month.add(sevenDays);
		model.addAttribute("matrix", month);

		// カレンダーにタスク表示する
		// 日付とタスクを紐付けるコレクション作成
		MultiValueMap<LocalDate, Tasks> tasks = new LinkedMultiValueMap<LocalDate, Tasks>();

		// リポジトリ経由でタスクを取得したものをlistに格納する（範囲は月の最初から最後+ログインしたユーザーの名前）
		List<Tasks> list;

		if (user.getAuthorities().stream().map(GrantedAuthority::getAuthority).anyMatch(a -> a.equals("ROLE_ADMIN"))) {
			list = repo.findAllByDateBetween(firstDay, endDay);

		} else {
			list = repo.findByDateBetween(firstDay, endDay, user.getName());
		}

		// listに格納したタスクを繰り返し処理でコレクションに追加する
		// <LocalDate,Tasks>:<(第1引数:タスクの日付),(第2引数:task)>
		for (Tasks task : list) {
			tasks.add(task.getDate(), task);
		}
		model.addAttribute("tasks", tasks);
		return "main";
	}

	// タスク登録ページの表示
	@GetMapping("/main/create/{date}") // {date}は可変
	public String create(Model model, @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
		return "create";
	}

	// タスク新規登録
	@PostMapping("/main/create")
	public String createPost(Model model, TaskForm post, @AuthenticationPrincipal AccountUserDetails user) {

		Tasks task = new Tasks(); // EntityクラスTasksのTasksメソッドをインスタンス化
		task.setName(user.getName());
		task.setTitle(post.getTitle());
		task.setText(post.getText());
		task.setDate(post.getDate());
		task.setDone(post.isDone());
		repo.save(task); // DBへ登録する

		return "redirect:/main";
	}

	// タスク編集・削除画面表示
	@GetMapping("/main/edit/{id}") // {id}は可変
	public String edit(Model model, @PathVariable Integer id) {
		Tasks task = repo.getById(id);
		model.addAttribute("task", task);
		return "edit";
	}

	// タスク編集
	@PostMapping("/main/edit/{id}")
	public String update(Model model, @Valid TaskForm update, @PathVariable Integer id,
			@AuthenticationPrincipal AccountUserDetails user) {
		Tasks task = new Tasks();
		task.setId(update.getId());
		task.setName(user.getName());
		task.setTitle(update.getTitle());
		task.setDate(update.getDate());
		task.setText(update.getText());
		task.setDone(update.isDone());
		model.addAttribute("task", task);
		repo.save(task); // DBに更新する

		return "redirect:/main";
	}

	// タスク削除
	@PostMapping("/main/delete/{id}")
	public String delete(@PathVariable Integer id) {
		repo.deleteById(id);
		return "redirect:/main";
	}
}
