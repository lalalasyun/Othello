package model;

import java.util.ArrayList;
import java.util.List;

public class Othello {
	// main
	int oth[][] = new int[8][8];
	int cnt = 0;
	boolean game = false;
	String record = "";

	Othello() {
	}

	public boolean getColor() {
		return cnt % 2 == 0 ? true : false;
	}

	public boolean isGame() {
		return game;
	}

	public void setGame(boolean game) {
		this.game = game;
	}

	public int[][] getOth() {
		return oth;
	}

	public String getRecord() {
		return record;
	}

	// オセロメソッド
	public String getStone() {

		search(cnt, oth);
		String str = oth();

		return str;
	}

	public String getStoneInit() {

		search(cnt, oth);
		String str = othInit();

		return str;
	}

	public boolean getPass() {
		search(cnt, oth);
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
		return false;
	}

	// 座標指定
	public boolean place(int x, int y) {
		if (getPass()) {
			return true;
		}
		search(cnt, oth);
		int count[] = count(oth);

		if (oth[x][y] == 3 || oth[x][y] == 4) {
			setRecord(x, y);
			put(x, y, cnt, oth);
			count = count(oth);
			if (count[1] + count[2] == 64) {
				game = false;
			}
			cnt++;
			return true;
		}
		return false;

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
	public String judge() {
		int point[] = count(oth);
		String str = "";
		if (point[1] == point[2]) {
			str = "draw";
		} else if (point[1] < point[2]) {
			str = "lose";
		} else {
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

	String othInit() {
		String str = "";
		for (int i = 0; i < 8; i++) {
			for (int n = 0; n < 8; n++) {
				if (oth[i][n] == 3 || oth[i][n] == 4) {
					str += 0;
				} else {
					str += oth[i][n];
				}

			}
		}
		return str;
	}

	// 配置
	int put(int a, int b, int cnt, int[][] oth) {
		int x, y;
		if (cnt % 2 == 0) {
			y = 1;
			x = 2;
		} else {
			y = 2;
			x = 1;
		}

		int i, n;

		int openness = 0;

		oth[a][b] = x;

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

						for (n = 1; n < i; n++) {
							oth[a - n][b] = x;
							openness += countOpenness(a - n, b, oth);
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

						for (n = 1; n < i; n++) {
							oth[a - n][b + n] = x;
							openness += countOpenness(a - n, b + n, oth);
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
						for (n = 1; n < i; n++) {
							oth[a][b + n] = x;
							openness += countOpenness(a, b + n, oth);
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
						for (n = 1; n < i; n++) {
							oth[a + n][b + n] = x;
							openness += countOpenness(a + n, b + n, oth);
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

						for (n = 1; n < i; n++) {
							oth[a + n][b] = x;
							openness += countOpenness(a + n, b, oth);
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

						for (n = 1; n < i; n++) {
							oth[a + n][b - n] = x;
							openness += countOpenness(a + n, b - n, oth);
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
						for (n = 1; n < i; n++) {
							oth[a][b - n] = x;
							openness += countOpenness(a, b - n, oth);
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
						for (n = 1; n < i; n++) {
							oth[a - n][b - n] = x;
							openness += countOpenness(a - n, b - n, oth);
						}
					}
				}
			}
		}

		return openness;

	}

	// 初期化
	public void initialize() {
		game = true;
		record = "";
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

	public void othelloAIPut(boolean turn) throws Exception {
		List<Integer> evaluation = othelloAI(turn, copyOth(oth));
		evaluation = getAIEvaluationRead(evaluation, turn);
		if (evaluation != null) {
			int[] coord = getAICoord(evaluation, oth);
			if (coord != null) {
				place(coord[0], coord[1]);
			} else {
				Thread.sleep(300);
				cnt++;
			}
		} else {
			Thread.sleep(300);
			cnt++;
		}
	}

	public String getAIEvaluation(boolean turn) {
		List<Integer> evaluation = othelloAI(turn, copyOth(oth));
		int[] evarank = new int[evaluation.size()];
		evaluation = getAIEvaluationRead(evaluation, turn);
		String mess = null;
		int evarankindex = 0;
		for (int search : evaluation) {
			for (int eva : evaluation) {
				if (search < eva) {
					evarank[evarankindex]++;
				}
			}
			evarankindex++;
		}

		if (evaluation != null) {
			mess = "eva,";
			mess += getColor() ? "3" : "4";

			for (int eva : evaluation) {
				mess += "," + eva*-1;
			}
		}
		return mess;
	}

	public int[] getAICoord(List<Integer> evaluation, int[][] oth) {
		List<int[]> coord = getCoord(oth);
		int minindex = 0, index = 0;
		for (int eva : evaluation) {
			if (eva < evaluation.get(minindex)) {
				minindex = index;
			}
			index++;
		}
		if (coord != null) {
			return coord.get(minindex);
		}
		return null;
	}

	public int readingAI(int[] coordcase, boolean turn) {
		int[][] copyOth = copyOth(oth);
		boolean aiturn = turn;
		put(coordcase[0], coordcase[1], aiturn ? 0 : 1, copyOth);
		search(aiturn ? 1 : 0, copyOth);
		aiturn = !aiturn;
		boolean end = false;
		while (true) {
			List<Integer> evaluation = othelloAI(aiturn, copyOth);
			int[] coord = getAICoord(evaluation, copyOth);
			if (coord != null) {
				put(coord[0], coord[1], aiturn ? 0 : 1, copyOth);
				search(aiturn ? 1 : 0, copyOth);
				end = false;
			}
			if (end) {
				return count(copyOth)[turn ? 1 : 2];
			}
			end = true;
			aiturn = !aiturn;
		}

	}

	public List<Integer> getAIEvaluationRead(List<Integer> evaluation, boolean turn) {
		List<int[]> coord = getCoord(oth);
		if (coord == null) {
			return evaluation;
		}
		if (count(oth)[1] + count(oth)[2] > 50) {
			int index = 0;
			for (int[] readCoord : coord) {
				Integer eva = evaluation.get(index) + readingAI(readCoord, turn) * -20;
				evaluation.set(index, eva);
				index++;
			}
		}
		return evaluation;
	}

	public List<Integer> othelloAI(boolean turn, int[][] oth) {
		List<int[]> coord = new ArrayList<>();
		List<Integer> evaluation = new ArrayList<>();
		int color = turn ? 1 : 2;
		int enemycolor = turn ? 2 : 1;
		// 評価関数
		int[][] stoneevaluation = { { 100, -40, 20, 5, 5, 20, -40, 100 }, { -40, -80, -1, -1, -1, -1, -80, -40 },
				{ 20, -1, 5, 1, 1, 5, -1, 20 }, { 5, -1, 1, 0, 0, 1, -1, 5 }, { 5, -1, 1, 0, 0, 1, -1, 5 },
				{ 20, -1, 5, 1, 1, 5, -1, 20 }, { -40, -80, -1, -1, -1, -1, -80, -40 },
				{ 100, -40, 20, 5, 5, 20, -40, 100 } };
		int[][] enemevaluation = { { 100, -40, 20, 5, 5, 20, -40, 100 }, { -40, -80, -1, -1, -1, -1, -80, -40 },
				{ 20, -1, 5, 1, 1, 5, -1, 20 }, { 5, -1, 1, 0, 0, 1, -1, 5 }, { 5, -1, 1, 0, 0, 1, -1, 5 },
				{ 20, -1, 5, 1, 1, 5, -1, 20 }, { -40, -80, -1, -1, -1, -1, -80, -40 },
				{ 100, -40, 20, 5, 5, 20, -40, 100 } };

		

		int[][] cornerscoord = { { 0, 0 }, { 7, 0 }, { 0, 7 }, { 7, 7 } };
		int[][] shift = { { 1, 1 }, { -1, 1 }, { 1, -1 }, { -1, -1 } };
		int index = 0;
		for (int[] corners : cornerscoord) {
			int x = corners[0];
			int y = corners[1];
			boolean ret = true;
			while (oth[x][y] == color) {
				x += shift[index][0];
				if (shift[index][0] == 1 && x == 5) {
					ret = true;
					break;
				}
				if (shift[index][0] == -1 && x == 3) {
					ret = true;
					break;
				}
				if(x == 7 || x == 0) {
					ret = true;
					break;
				}
				ret = false;
			}
			if (!ret) {
				ret = true;
				while (oth[x][y] == enemycolor) {
					x += shift[index][0];
					
					if(x == 7 || x == 0) {
						ret = true;
						break;
					}
					
					ret = false;
				}
			}
			if (!ret) {
				stoneevaluation[x][y] = 150;
				if (shift[index][0] == 1 && x == 6) {
					stoneevaluation[7][corners[1]] = 0;
				}
				if (shift[index][0] == -1 && x == 1) {
					stoneevaluation[0][corners[1]] = 0;
				}
			}
			index++;
		}
		index = 0;
		for (int[] corners : cornerscoord) {
			int x = corners[0];
			int y = corners[1];
			boolean ret = true;
			while (oth[x][y] == color) {
				y += shift[index][1];

				if (shift[index][1] == 1 && y == 5) {
					ret = true;
					break;
				}

				if (shift[index][1] == -1 && y == 3) {
					ret = true;
					break;
				}

				ret = false;
			}
			if (!ret) {
				ret = true;
				while (oth[x][y] == enemycolor) {
					y += shift[index][1];
					
					if(y == 7 || y == 0) {
						ret = true;
						break;
					}
					ret = false;
				}
			}
			if (!ret) {
				if (shift[index][1] == 1) {
					stoneevaluation[corners[0]][7] = 0;
				}
				if (shift[index][1] == -1) {
					stoneevaluation[corners[0]][0] = 0;
				}
				stoneevaluation[x][y] = 150;
			}
			index++;
		}

		coord = getCoord(oth);
		if (coord == null) {
			return evaluation;
		}
		for (int[] ary : coord) {
			int[][] copyoth = copyOth(oth);
			int oppennes = put(ary[0], ary[1], turn ? 0 : 1, copyoth);
			search(turn ? 1 : 0, copyoth);
			List<int[]> getcoord = getCoord(copyoth);
			int point = 0;
			if (getcoord != null) {
				for (int[] getary : getcoord) {
					point += enemevaluation[getary[0]][getary[1]];
					put(getary[0], getary[1], turn ? 1 : 0, copyoth);
					search(turn ? 0 : 1, copyoth);
					if(getCoord(copyoth)==null) {
						point+=100;
					}
				}
			} else {
				point = -200;
			}
			
			evaluation.add((oppennes * 7) + point + (stoneevaluation[ary[0]][ary[1]] * -1));

		}
		return evaluation;
	}

	public List<int[]> getCoord(int[][] oth) {
		List<int[]> coord = new ArrayList<>();
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
			return null;
		}
		return coord;
	}

	public int countOpenness(int x, int y, int[][] oth) {
		int[][] ary = { { -1, 1 }, { 0, 1 }, { 1, 1 }, { 1, 0 }, { 1, -1 }, { 0, -1 }, { -1, -1 }, { -1, 0 } };
		int count = 0;
		for (int[] Coord : ary) {
			int CoordX = x + Coord[0];
			int CoordY = y + Coord[1];
			if (CoordX > -1 && CoordX < 8 && CoordY > -1 && CoordY < 8) {

				int type = oth[CoordX][CoordY];
				if (type != 1 && type != 2) {

					count++;
				}
			}
		}
		return count;
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

	void setRecord(int x, int y) {
		String charx = Integer.toString(x);
		String chary = Integer.toString(y);
		record += charx + chary;
	}

}
