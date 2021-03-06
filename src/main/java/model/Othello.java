package model;

import java.util.ArrayList;
import java.util.List;

public class Othello {
	// main
	int oth[][] = new int[8][8];
	int cnt = 0;
	boolean game = false;
	boolean AIread = false;
	String record = "";

	int readlevel = 0;
	int[][] stoneevaluation;
	int[][] initeva = { { 600, -40, 20, 5, 5, 20, -40, 600 }, { -40, -100, -1, -1, -1, -1, -100, -40 },
			{ 20, -1, 5, 1, 1, 5, -1, 20 }, { 5, -1, 1, 0, 0, 1, -1, 5 }, { 5, -1, 1, 0, 0, 1, -1, 5 },
			{ 20, -1, 5, 1, 1, 5, -1, 20 }, { -40, -100, -1, -1, -1, -1, -100, -40 },
			{ 600, -40, 20, 5, 5, 20, -40, 600 } };

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

		stoneevaluation = initeva;
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

	public int[] othelloAIPut(boolean turn) throws Exception {
		readlevel = 20;
		List<Integer> evaluation = othelloAI(turn, copyOth(oth));
		evaluation = getAIEvaluationRead(evaluation, turn, oth, false);
		if (evaluation != null) {
			int[] coord = getAICoord(evaluation, oth);
			if (coord != null) {
				place(coord[0], coord[1]);
				return coord;
			} else {
				Thread.sleep(300);
				cnt++;
			}
		} else {
			Thread.sleep(300);
			cnt++;
		}
		return null;
	}

	public String getAIEvaluation(boolean turn) {

		readlevel = 20;

		List<Integer> evaluation = othelloAI(turn, copyOth(oth));
		evaluation = getAIEvaluationRead(evaluation, turn, oth, true);

		String mess = null;

		if (evaluation != null) {
			mess = "eva,";
			mess += getColor() ? "3" : "4";

			for (int eva : evaluation) {
				if (eva > -1) {
					mess += ",+" + eva;
				} else {
					mess += "," + eva;
				}

			}
		}
		return mess;
	}

	public int[] getAICoord(List<Integer> evaluation, int[][] oth) {
		List<int[]> coord = getCoord(oth);
		int maxindex = 0, index = 0;
		for (int eva : evaluation) {
			if (eva > evaluation.get(maxindex)) {
				maxindex = index;
			}
			index++;
		}
		if (coord != null) {
			return coord.get(maxindex);
		}
		return null;
	}

	public int readingAI(int pointcase, int[] coordcase, boolean turn, boolean navi) {
		int[][] copyOth = copyOth(oth);
		boolean aiturn = turn;
		int endcnt = 0, readcnt = 0;
		int enempoint = 0;

		put(coordcase[0], coordcase[1], aiturn ? 0 : 1, copyOth);
		search(aiturn ? 1 : 0, copyOth);
		aiturn = !aiturn;
		while (true) {
			List<Integer> evaluation = othelloAI(aiturn, copyOth);
			if (evaluation.size() != 0) {
				int[] coord = getAICoord(evaluation, copyOth);
				if (coord != null) {
					put(coord[0], coord[1], aiturn ? 0 : 1, copyOth);
					search(aiturn ? 1 : 0, copyOth);
					endcnt = 0;
					readcnt++;
					aiturn = !aiturn;
				}
			} else {
				aiturn = !aiturn;
				search(aiturn ? 1 : 0, copyOth);
				
				endcnt++;
			}
			if (navi && endcnt == 2) {
				int point = (count(copyOth)[turn ? 2 : 1] - count(copyOth)[!turn ? 2 : 1]);
				return point;
			}

			if ((readcnt > readlevel || endcnt == 2) && !navi) {
				int point = 0;
				for (int i = 0; i < 8; i++) {
					for (int n = 0; n < 8; n++) {
						if (copyOth[i][n] == (!turn ? 2 : 1)) {
							enempoint -= stoneevaluation[i][n];
						}
					}
				}
			
				
				int turncount = count(oth)[1] + count(oth)[2];
				if (turncount > 50) {
					point = (count(copyOth)[turn ? 2 : 1] - count(copyOth)[!turn ? 2 : 1]) * 10;
				}

				if (turncount > 59) {

					return point;
				}

				enempoint *= 2;
				return pointcase + enempoint + point;
			}
		}

	}

	public List<Integer> getAIEvaluationRead(List<Integer> evaluation, boolean turn, int[][] oth, boolean navi) {
		search(turn ? 0 : 1, oth);
		List<int[]> coord = getCoord(oth);
		if (coord == null) {
			return evaluation;
		}

		int index = 0;
		for (int[] readCoord : coord) {
			Integer eva = readingAI(evaluation.get(index), readCoord, turn, navi);
			evaluation.set(index, eva);
			index++;
		}

		return evaluation;
	}

	public List<Integer> othelloAI(boolean turn, int[][] oth) {
		List<int[]> coord = new ArrayList<>();
		List<Integer> evaluation = new ArrayList<>();
		
		search(turn ? 0 : 1, oth);
		coord = getCoord(oth);
		if (coord == null) {
			return evaluation;
		}
		for (int[] ary : coord) {
			int point = 0;
			if (stoneevaluation[ary[0]][ary[1]] == -100) {
				point = -500;
			}
			if (stoneevaluation[ary[0]][ary[1]] == 600) {
				point = -2000;
				int[][] shift = { { 1, 0 }, { 0, 1 }, { -1, 0 }, { 0, -1 } };
				int cnt = 0;
				for (int[] s : shift) {
					
					boolean ret1 = ary[0] + s[0] > -1 && ary[0] + s[0] < 8;
					boolean ret2 = ary[1] + s[1] > -1 && ary[1] + s[1] < 8;
					if (ret1 && ret2) {
						search(turn ? 1 : 0, oth);
						int search = oth[ary[0] + s[0]][ary[1] + s[1]];
						if (search != (!turn?4:3)) {
							cnt++;
							if (cnt == 2) {
								point = 0;
							}
						}
					}
				}
				search(turn ? 0 : 1, oth);
			}

			int[][] copyoth = copyOth(oth);
			int oppennes = put(ary[0], ary[1], turn ? 0 : 1, copyoth) * -100;
			search(turn ? 1 : 0, copyoth);
			List<int[]> getcoord = getCoord(copyoth);

			int enempoint = 0;
			int enemcount = 0;
			int enemoppens = 0;

			if (count(copyoth)[!turn ? 2 : 1] == 0) {
				enempoint -= 500;
			}
			if (getcoord != null) {
				enemoppens = 10;
				for (int[] getary : getcoord) {
					enempoint += stoneevaluation[getary[0]][getary[1]];
					int[][] enemoth = copyOth(copyoth);
					int oppen = put(getary[0], getary[1], turn ? 1 : 0, enemoth);
					enemoppens = enemoppens > oppen ? oppen : enemoppens;
				}
				enemcount = getcoord.size() * -100;
				enemoppens *= 100;
			}
			enempoint *= -2;

			int addpoint = point + oppennes + enemoppens + enempoint + enemcount;
			evaluation.add(addpoint);

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
		for (int[] shift : ary) {
			int CoordX = x + shift[0];
			int CoordY = y + shift[1];
			if (CoordX > -1 && CoordX < 8 && CoordY > -1 && CoordY < 8) {

				int type = oth[CoordX][CoordY];
				if (type != 1 && type != 2) {
					count++;
				}
			}
		}
		return count;
	}

	public int countOuterStone(boolean turn, int[][] oth) {
		int color = turn ? 2 : 1;
		int count = 0;
		for (int index = 0; index < 4; index++) {
			int addcnt = 0;
			for (int i = 0; i < 8; i++) {
				int[] aryY = { 0, i, i, 7 };
				int[] aryX = { i, 0, 7, i };
				int y = aryY[index];
				int x = aryX[index];
				if (!(oth[y][x] == 1 || oth[y][x] == 2)) {
					addcnt = 0;
					break;
				}
				if (oth[y][x] == color) {
					addcnt++;
				}
			}
			count += addcnt;
		}
		int[][] arycoord = { { 0, 0 }, { 0, 7 }, { 7, 0 }, { 7, 7 } };
		int index = 0;
		for (int[] coord : arycoord) {
			if (oth[coord[0]][coord[1]] == color) {
				count++;
				for (int i = 1; i < 4; i++) {
					int[] aryY1 = { 0, 0, 7, 7 };
					int[] aryX1 = { i, 7 - i, i, 7 - i };
					if (oth[aryY1[index]][aryX1[index]] == color) {
						count++;
					}
					int[] aryY2 = { i, i, 7 - i, 7 - i };
					int[] aryX2 = { 0, 7, 0, 7 };
					if (oth[aryY2[index]][aryX2[index]] == color) {
						count++;
					}
				}
				index++;
			}
		}

		return count;
	}
	
	public int ConfirmStone(boolean turn, int[][] oth) {
		int color = turn ? 2 : 1;
		
		boolean[][] confirm = new boolean[8][8];
		for (int index = 0; index < 4; index++) {
			int addcnt = 0;
			List<int []> list = new ArrayList<>();
			for (int i = 0; i < 8; i++) {
				int[] aryY = { 0, i, i, 7 };
				int[] aryX = { i, 0, 7, i };
				int y = aryY[index];
				int x = aryX[index];
				if (!(oth[y][x] == 1 || oth[y][x] == 2)) {
					addcnt = 0;
					break;
				}
				if (oth[y][x] == color) {
					addcnt++;
					int[] addcoord = {y,x};
					list.add(addcoord);
				}
			}
			if(addcnt != 0) {
				for(int[] coord:list) {
					confirm[coord[0]][coord[1]] = true;
				}
			}
		}
		int[][] arycoord = { { 0, 0 }, { 0, 7 }, { 7, 0 }, { 7, 7 } };
		int index = 0;
		for (int[] coord : arycoord) {
			if (oth[coord[0]][coord[1]] == color) {
				for (int i = 1; i < 4; i++) {
					int[] aryY1 = { 0, 0, 7, 7 };
					int[] aryX1 = { i, 7 - i, i, 7 - i };
					if (oth[aryY1[index]][aryX1[index]] == color) {
						confirm[index][aryX1[index]] = true;
					}
					int[] aryY2 = { i, i, 7 - i, 7 - i };
					int[] aryX2 = { 0, 7, 0, 7 };
					if (oth[aryY2[index]][aryX2[index]] == color) {
						confirm[aryY2[index]][aryX2[index]] = true;
					}
				}
				index++;
			}
		}
		
		
		
		
		
		return 0;
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
