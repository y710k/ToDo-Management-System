package com.dmm.task.controller;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

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
		System.out.println(month);

		model.addAttribute("month", month);

		return "/main";

	}

}
