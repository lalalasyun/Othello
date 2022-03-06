package sample.websocket;

import java.util.ArrayList;
import java.util.List;

public class Othello {
	// main
	int mode = 0;
	int oth[][] = new int[8][8];
	int cnt = 0;
	boolean game = true;

	Othello(int mode) {
		this.mode = mode;
	}

	boolean getColor() {
		return cnt % 2 == 0 ? true : false;
	}

	public boolean isGame() {
		return game;
	}

	// オセロメソッド
	String othello() {

		search(cnt, oth);
		String str = oth();

		return str;
	}

	// 座標指定
	boolean place(int x, int y) {
		int count[] = count(oth);

		if (cnt % 2 == 0 && count[3] == 0) {
			cnt++;
			search(1, oth);
			if (count(oth)[4] == 0) {
				game = false;
			}
			return true;
		} else if (cnt % 2 == 1 && count[4] == 0) {
			cnt++;
			search(0, oth);
			if (count(oth)[3] == 0) {
				game = false;
			}
			return true;

		}

		if (oth[x][y] == 3 || oth[x][y] == 4) {
			put(x, y, cnt, oth);
			cnt++;
			if(count(oth)[0] == 64) {
				game = false;
			}
			return true;
		} else {
			return false;
		}

	}

	// カウント
	int[] count(int oth[][]) {
		int cnt[] = new int[5];
		for (int i = 0; i < 8; i++) {
			for (int n = 0; n < 8; n++) {
				if (oth[i][n] == 0) {
					cnt[0]++;
				} else if (oth[i][n] == 1) {
					cnt[1]++;
				} else if (oth[i][n] == 2) {
					cnt[2]++;
				} else if (oth[i][n] == 3) {
					cnt[3]++;
				} else if (oth[i][n] == 4) {
					cnt[4]++;
				}
			}
		}
		return cnt;
	}
	
	// 勝敗
	String judge() {
		int point[] = count(oth);
		String str = "";
		if(point[1] == point[2]) {
			str = "draw";
		}else if(point[1] < point[2]){
			str = "lose";
		}else {
			str = "win";
		}
		
		return str;
	}

	// 検索
	void search(int cnt, int[][] oth) {
		for (int a = 0; a < 8; a++) {
			for (int b = 0; b < 8; b++) {
				if (oth[a][b] == 3 || oth[a][b] == 4) {
					oth[a][b] = 0;
				}
			}
		}
		int i, bl, wh, x;
		if (cnt % 2 == 0) {
			bl = 2;
			wh = 1;
			x = 3;
		} else {
			bl = 1;
			wh = 2;
			x = 4;
		}
		for (int a = 0; a < 8; a++) {
			for (int b = 0; b < 8; b++) {
				if (oth[a][b] == bl) {
					// 0度
					if (a - 1 >= 0) {
						if (oth[a - 1][b] == wh) {
							for (i = 1; oth[a - i][b] == wh;) {
								i++;
								if (a - i < 0) {
									break;
								}
							}
							if (a - i > -1) {
								if (oth[a - i][b] == 0 || oth[a - i][b] == x) {
									oth[a - i][b] = x;
								}
							}
						}
					}

					// 45度
					if (a - 1 >= 0 && b + 1 <= 7) {
						if (oth[a - 1][b + 1] == wh) {
							for (i = 1; oth[a - i][b + i] == wh;) {
								i++;
								if (a - i < 0 || b + i > 7) {
									break;
								}

							}
							if (a - i > -1 && b + i < 8) {
								if (oth[a - i][b + i] == 0 || oth[a - i][b + i] == x) {
									oth[a - i][b + i] = x;
								}
							}
						}
					}
					// 90度
					if (b + 1 <= 7) {
						if (oth[a][b + 1] == wh) {
							for (i = 1; oth[a][b + i] == wh;) {
								i++;
								if (b + i > 7) {
									break;
								}
							}
							if (b + i < 8) {
								if (oth[a][b + i] == 0 || oth[a][b + i] == x) {
									oth[a][b + i] = x;
								}
							}
						}
					}

					// 135度
					if (a + 1 <= 7 && b + 1 <= 7) {
						if (oth[a + 1][b + 1] == wh) {
							for (i = 1; oth[a + i][b + i] == wh;) {
								i++;
								if (a + i > 7 || b + i > 7) {
									break;
								}

							}
							if (a + i < 8 && b + i < 8) {
								if (oth[a + i][b + i] == 0 || oth[a + i][b + i] == x) {
									oth[a + i][b + i] = x;
								}
							}
						}
					}

					// 180度
					if (a + 1 <= 7) {
						if (oth[a + 1][b] == wh) {
							for (i = 1; oth[a + i][b] == wh;) {
								i++;
								if (a + i > 7) {
									break;
								}

							}
							if (a + i < 8) {
								if (oth[a + i][b] == 0 || oth[a + i][b] == x) {
									oth[a + i][b] = x;
								}
							}
						}

					}
					// 225度
					if (a + 1 <= 7 && b - 1 >= 0) {
						if (oth[a + 1][b - 1] == wh) {
							for (i = 1; oth[a + i][b - i] == wh;) {
								i++;
								if (a + i > 7 || b - i < 0) {
									break;
								}

							}
							if (a + i < 8 && b - i > -1) {
								if (oth[a + i][b - i] == 0 || oth[a + i][b - i] == x) {
									oth[a + i][b - i] = x;
								}
							}
						}
					}

					// 270度
					if (b - 1 >= 0) {
						if (oth[a][b - 1] == wh) {
							for (i = 1; oth[a][b - i] == wh;) {
								i++;
								if (b - i < 0) {
									break;
								}

							}
							if (b - i > -1) {
								if (oth[a][b - i] == 0 || oth[a][b - i] == x) {
									oth[a][b - i] = x;
								}
							}
						}
					}

					// 315度
					if (a - 1 >= 0 && b - 1 >= 0) {
						if (oth[a - 1][b - 1] == wh) {
							for (i = 1; oth[a - i][b - i] == wh;) {
								i++;
								if (a - i < 0 || b - i < 0) {
									break;
								}

							}
							if (a - i > -1 && b - i > -1) {
								if (oth[a - i][b - i] == 0 || oth[a - i][b - i] == x) {
									oth[a - i][b - i] = x;
								}
							}
						}
					}

				}
			}
		}
	}

	// スクリーン
	String oth() {

		String str = "";
		for (int i = 0; i < 8; i++) {
			for (int n = 0; n < 8; n++) {
				str += oth[i][n];
			}
		}
		return str;
	}

	// 配置
	void put(int a, int b, int cnt, int[][] oth) {
		int x, y;
		if (cnt % 2 == 0) {
			y = 1;
			x = 2;
		} else {
			y = 2;
			x = 1;
		}

		int i, n;

		// 0度
		if (a - 1 >= 0) {
			if (oth[a - 1][b] == y) {
				for (i = 1; oth[a - i][b] == y;) {
					i++;
					if (a - i < 0) {
						break;
					}
				}
				if (a - i >= 0) {
					if (oth[a - i][b] == x) {

						for (n = 0; n < i + 1; n++) {
							oth[a - n][b] = x;
						}
					}

				}

			}
		}

		// 45度
		if (a - 1 >= 0 && b + 1 <= 7) {
			if (oth[a - 1][b + 1] == y) {
				for (i = 1; oth[a - i][b + i] == y;) {
					i++;
					if (a - i < 0 || b + i > 7) {
						break;
					}
				}
				if (a - i >= 0 && b + i <= 7) {
					if (oth[a - i][b + i] == x) {

						for (n = 0; n < i + 1; n++) {
							oth[a - n][b + n] = x;
						}
					}
				}

			}
		}
		// 90度
		if (b + 1 <= 7) {
			if (oth[a][b + 1] == y) {
				for (i = 1; oth[a][b + i] == y;) {
					i++;
					if (b + i > 7) {
						break;
					}
				}
				if (b + i <= 7) {
					if (oth[a][b + i] == x) {
						for (n = 0; n < i + 1; n++) {
							oth[a][b + n] = x;
						}

					}

				}
			}
		}
		// 135度
		if (a + 1 <= 7 && b + 1 <= 7) {
			if (oth[a + 1][b + 1] == y) {
				for (i = 1; oth[a + i][b + i] == y;) {
					i++;
					if (a + i > 7 || b + i > 7) {
						break;
					}

				}
				if (a + i <= 7 && b + i <= 7) {
					if (oth[a + i][b + i] == x) {
						for (n = 0; n < i + 1; n++) {
							oth[a + n][b + n] = x;
						}

					}

				}
			}
		}
		// 180度
		if (a + 1 <= 7) {
			if (oth[a + 1][b] == y) {
				for (i = 1; oth[a + i][b] == y;) {
					i++;
					if (a + i > 7) {
						break;
					}
				}
				if (a + i <= 7) {
					if (oth[a + i][b] == x) {

						for (n = 0; n < i + 1; n++) {
							oth[a + n][b] = x;
						}

					}

				}
			}
		}
		// 225度
		if (a + 1 <= 7 && b - 1 >= 0) {
			if (oth[a + 1][b - 1] == y) {
				for (i = 1; oth[a + i][b - i] == y;) {
					i++;
					if (a + i > 7 || b - i < 0) {
						break;
					}
				}
				if (a + i <= 7 && b - i >= 0) {
					if (oth[a + i][b - i] == x) {

						for (n = 0; n < i + 1; n++) {
							oth[a + n][b - n] = x;
						}

					}

				}
			}
		}
		// 270度
		if (b - 1 >= 0) {
			if (oth[a][b - 1] == y) {
				for (i = 1; oth[a][b - i] == y;) {
					i++;
					if (b - i < 0) {
						break;
					}
				}
				if (b - i >= 0) {
					if (oth[a][b - i] == x) {
						for (n = 0; n < i + 1; n++) {
							oth[a][b - n] = x;
						}

					}
				}
			}
		}
		// 315度
		if (a - 1 >= 0 && b - 1 >= 0) {
			if (oth[a - 1][b - 1] == y) {
				for (i = 1; oth[a - i][b - i] == y;) {
					i++;
					if (a - i < 0 || b - i < 0) {
						break;
					}
				}
				if (a - i >= 0 && b - i >= 0) {
					if (oth[a - i][b - i] == x) {
						for (n = 0; n < i + 1; n++) {
							oth[a - n][b - n] = x;
						}
					}
				}
			}
		}

	}

	// 初期化
	void initialize() {
		game = true;
		cnt = 0;
		for (int a = 0; a < 8; a++) {
			for (int b = 0; b < 8; b++) {
				oth[a][b] = 0;
			}
		}

		oth[3][3] = 1;
		oth[3][4] = 2;
		oth[4][3] = 2;
		oth[4][4] = 1;
	}

	void othelloAI() {
		List<int[]> coord = new ArrayList<>();
		List<Integer> evaluation = new ArrayList<>();
		int[][] stoneevaluation = { {100, -40, 20, 5, 5, 20, -40, 100}, {-40, -80, -1, -1, -1, -1, -80, -40}, {20, -1, 5, 1, 1,
				5, -1, 20},{ 5, -1, 1, 0, 0, 1, -1, 5}, {5, -1, 1, 0, 0, 1, -1, 5},{ 20, -1, 5, 1, 1, 5, -1, 20}, {-40, -80, -1,
				-1, -1, -1, -80, -40}, {100, -40, 20, 5, 5, 20, -40, 100} };

		int maxindex = 0;
		int move = 0;
		for (int i = 0; i < 8; i++) {
			for (int n = 0; n < 8; n++) {
				if (oth[i][n] == 3 || oth[i][n] == 4) {
					int[] ary = { i, n };
					coord.add(ary);
					move++;
				}
			}
		}
		if (move == 0) {
			cnt++;
			return;
		}
		int index = 0;
		for (int[] ary : coord) {
			int[][] copyoth = copyOth(oth);
			int stonepoint = 0;
			put(ary[0], ary[1], 1, copyoth);
			search(0, copyoth);
			
			for (int i = 0; i < 8; i++) {
				for (int n = 0; n < 8; n++) {
					if (copyoth[i][n] == 3 || copyoth[i][n] == 4) {
						stonepoint += stoneevaluation[i][n];
					}
				}
			}
			int[] count = count(copyoth);
			evaluation.add(stoneevaluation[ary[0]][ary[1]] + (stonepoint * -1) +  ((move - count[3] - count[4]) + 1)*10);
			if (evaluation.get(maxindex) <= evaluation.get(index)) {
				maxindex = index;
			}
			index++;
		}

		place(coord.get(maxindex)[0], coord.get(maxindex)[1]);
		System.out.println(evaluation);
		System.out.println(maxindex);
	}

	int[][] copyOth(int[][] oth) {
		int[][] copyoth = new int[8][8];
		for (int i = 0; i < 8; i++) {
			for (int n = 0; n < 8; n++) {
				copyoth[i][n] = oth[i][n];
			}
		}
		return copyoth;
	}

}
