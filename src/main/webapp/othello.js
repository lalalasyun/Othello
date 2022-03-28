// let url = "ws://lalalasyun.com/OthelloWeb/othello";
let url = "wss://othellojp.herokuapp.com/othello";
var ws;

var wsize;
var turn;
var gameturn = true;
var game = false;
var cntblack = 0;
var cntwhite = 0;
var formIsEmpty;
var putcoord = [];
var ainavi = true;
var kihuindex = 0;
var tweetinit = 'オセロをプレイしました';
var tweet = tweetinit;
//document.getElementById
var resultbox, result, black, white;

var gamebtn, start, online, assistbtn,changeturn, kihubtn, login, logout, userlog;

var form, userid, userpass, kihumenu, kihuplaybtn, tweetmess;

var turn1, turn2, userdata, username1, username2, userrate1, userrate2;

function load() {
	documentload();
	screenset();
	initStone();
	connect();
}


function Twitter() {
	openTwitter(tweet);
}
//openTwitter(投稿文、シェアするURL)

function openTwitter(text) {
	// var gameurl = 'http://lalalasyun.com/OthelloWeb/';
	var gameurl = 'https://othellojp.herokuapp.com/';
	var turl = "https://twitter.com/intent/tweet?text=" + text + "&url=" + gameurl;
	window.open(turl, '_blank');
}

function documentload() {
	resultbox = document.getElementById('resultbox');
	result = document.getElementById('result');
	black = document.getElementById("black");
	white = document.getElementById("white");
	gamebtn = document.getElementById('gamebtn');
	start = document.getElementById('startid');
	online = document.getElementById('online');
	assistbtn = document.getElementById('assistbtn');
	changeturn = document.getElementById('changeturn');
	kihubtn = document.getElementById('kihubtn');
	login = document.getElementById('login');
	logout = document.getElementById('logout');
	userlog = document.getElementById('usermenu');
	userid = document.getElementById("id");
	userpass = document.getElementById("pass");
	kihuplaybtn = document.getElementById("kihuplaybtn");
	tweetmess = document.getElementById("tweetmess");
	form = document.getElementById('form');
	kihumenu = document.getElementById('kihumenu');
	turn1 = document.getElementById('turn1');
	turn2 = document.getElementById('turn2');
	username1 = document.getElementById('username1');
	username2 = document.getElementById('username2');
	userrate1 = document.getElementById('userrate1');
	userrate2 = document.getElementById('userrate2');
	userdata = document.getElementById('userdata');
}

function stoneClick(x, y) {
	if (game && gameturn) {
		ws.send("coord," + x + "," + y);
		gameturn = false;
	}
}

function startbtn() {
	ws.send(start.innerHTML == "スタート" ? "start" : "reset");
	form.hidden = true;
	if (start.innerHTML == "スタート") {
		kihubtn.disabled = false;
		resultbox.hidden = false;
		userdata.hidden = false;
		kihumenu.hidden = true;
		kihuplaybtn.hidden = true;
		assistbtn.disabled = true;
	}
}

function reset() {
	game = false;
	changeturn.disabled = false;
	result.innerHTML = "";
	black.innerHTML = "";
	white.innerHTML = "";
	start.innerHTML = "スタート";
	kihuplaybtn.hidden = true;
	kihubtn.disabled = false;
	tweetmess.hidden = true;
	assistbtn.disabled = true;
	tweet = tweetinit;
	initStone();
}

function onlinebtn() {
	userlog.innerHTML = "";
	game = false;
	if (online.innerHTML == "オフライン") {
		onlinestart();
		ws.send("online");
	} else {
		offlinestart();
		ws.send("offline");
	}
}
function onlinestart() {
	reset();
	online.innerHTML = "オンライン"
	userlog.innerHTML = "プレイヤーを待っています";
	start.disabled = true;
	userdata.hidden = true;
	black.innerHTML = "";
	white.innerHTML = "";
}

function offlinestart() {
	reset();
	online.innerHTML = "オフライン"
	userdata.hidden = false;
	userlog.innerHTML = "";
	start.disabled = false;
}

function account() {
	userlog.innerHTML = "";
	userid.value = "";
	userpass.value = "";
	var hidden = form.hidden ? false : true;
	form.hidden = hidden

	resultbox.hidden = !hidden;
	if (kihubtn.innerHTML != '棋譜終了') {
		userdata.hidden = !hidden;
	}
	inputChange();
}

function loginbtn() {
	if (formIsEmpty) {
		ws.send("login," + userid.value + "," + userpass.value);
		formclose();
		return;
	}
	alert("フォームを入力してください");
}

function logoutbtn() {
	ws.send("logout");
	formclose();
	return;
}

function registerbtn() {
	if (formIsEmpty) {
		ws.send("register," + userid.value + "," + userpass.value);
		formclose();
		return;
	}
	alert("フォームを入力してください");
}

function deletebtn() {
	if (formIsEmpty) {
		ws.send("delete," + userid.value + "," + userpass.value);
		formclose();
		return;
	}
	alert("フォームを入力してください");
}

function aiassist() {
	ainavi = !ainavi;
	if (ainavi) {
		initStone();
	}
	ws.send('ainavi');

}

function kihu() {
	if (kihumenu.hidden == true) {
		kihuindex = 0;
		kihumenu.innerHTML = "";
		kihubtn.innerHTML = "棋譜終了"
		userdata.hidden = true;
		resultbox.hidden = true;
		userlog.innerHTML = "SQLサーバーに接続しています";
		ws.send('kihu,get');
		kihumenu.hidden = false;
	} else {
		kihubtn.innerHTML = "棋譜データ"
		gamebtn.hidden = false;
		kihuplaybtn.hidden = true;
		resultbox.hidden = false;
		userdata.hidden = false;
		kihumenu.hidden = true;
		ainavi = true;
		initStone();
	}
}

function kihunext() {
	ws.send('kihu,next');
}

function kihuback() {
	ws.send('kihu,back');
}

function kihustart() {
	ws.send('kihu,start');
}

function kihuend() {
	ws.send('kihu,end');
}

function OnLinkClick(index) {
	userlog.innerHTML = '計算中...';
	ws.send('kihu,index,' + index);
}

function formclose() {
	if (kihubtn.innerHTML != '棋譜終了') {
		userdata.hidden = false;
		form.hidden = true;
		resultbox.hidden = false;
		userlog.innerHTML = "SQLサーバーに接続しています";
	}

}

function inputChange() {
	formIsEmpty = !(userid.value == "" || userpass.value == "");
}

function changeturnbtn() {
	ws.send("changeturn");
}
function connect() {
	ws = new WebSocket(url);
	ws.onopen = function () {
		userlog.innerHTML = "サーバーに接続しました。<br>";
	}

	ws.onmessage = function (receive) {
		var ary = receive.data.split(',');
		var command = ary[0];
		var log = ['ログイン', 'ログアウト', '登録', '削除'];
		var mess = ['しました', 'に失敗しました'];
		switch (command) {
			case "stone":
				stone(ary[1]);
				break;
			case "coord":
				putStone(Number(ary[1]), Number(ary[2]), 6);
				break;
			case "miss":
				gameturn = true;
				break;
			case "eva":
				if(kihumenu.hidden == true && !gameturn){
					break;
				}
				var type = Number(ary[1]);
				var maxeva = -10000;
				var maxX, maxY;
				for (var index in putcoord) {
					var evaindex = Number(index) + 2;
					putEva(putcoord[index][0], putcoord[index][1], type, ary[evaindex]);
					if (Number(ary[evaindex]) > Number(maxeva)) {
						maxeva = Number(ary[evaindex]);
						maxX = putcoord[index][0];
						maxY = putcoord[index][1];
					}
				}
				putStone(maxX, maxY, 5);
				break;
			case "turn":
				turn = ary[1] == "black" ? true : false;
				turn1.innerHTML = turn ? "先手:" : "後手:";
				turn2.innerHTML = !turn ? "先手:" : "後手:";
				break;
			case "matching":
				start.disabled = false;
				if (!game && ary[1] != "AI") {
					userlog.innerHTML = "プレイヤーが見つかりました";
					userdata.hidden = false;
				}
				if (ary[1] == "wait") {
					onlinestart();
				}
				break;
			case "name":
				username1.innerHTML = turn ? ary[1] : ary[2];
				username2.innerHTML = turn ? ary[2] : ary[1];
				break;
			case "rate":
				var aryuser = [userrate1, userrate2];
				if (ary[1] == "") {
					aryuser[turn ? 1 : 0].innerHTML = ary[2];
				} else {
					aryuser[turn ? 0 : 1].innerHTML = ary[1];
				}
				break;
			case "login":
				var index = Number(ary[1]);
				if (index == 0) {
					if (ary[2] == "success") {
						if (online.innerHTML == "オンライン") {
							ws.send("online");
						}
						login.disabled = true;
						logout.disabled = false;
					}
				} else if (index == 1) {
					if (ary[2] == "success") {
						login.disabled = false;
						logout.disabled = true;
					}
				}
				userlog.innerHTML = ary[2] == "success" ? log[index] + mess[0] : log[index] + mess[1];
				userlog.innerHTML += "<br>";
				break;
			case "start":
				kihubtn.disabled = true;
				userlog.innerHTML = "";
				game = true;
				changeturn.disabled = true;
				assistbtn.disabled = false;
				start.innerHTML = "リセット";
				initStone();
				break;
			case "reset":
				reset();
				break;
			case "end":
				var mess = "データ取得中...";
				userrate1.innerHTML = mess;
				userrate2.innerHTML = mess;
				tweet = black.innerHTML + white.innerHTML;
				var jodge = winresult();
				tweet += "で";
				if (username2.innerHTML == 'AI') {
					tweet += "AIに";
				}
				var resmess = jodge == 0 ? "勝ちました" : jodge == 1 ? "負けました" : "引き分けです";
				tweet += resmess;
				tweetmess.hidden = false;
				changeturn.disabled = false;
				break;
			case "kihu":
				if (ary[1] == "failure") {
					userlog.innerHTML = "棋譜データが見つかりませんでした";
					kihumenu.hidden = true;
					userdata.hidden = false;
					kihumenu.innerHTML = "";
					kihuplaybtn.hidden = true;
					kihubtn.innerHTML = "棋譜データ"
					gamebtn.hidden = false;
				} else if (ary[1] == "play") {
					userlog.innerHTML = "";
					kihuplaybtn.hidden = false;
					kihubtn.innerHTML = "棋譜終了"
					gamebtn.hidden = true;
					ainavi = false;
				} else {
					userlog.innerHTML = "";
					kihumenu.hidden = false;
					resultbox.hidden = true;
					kihuplaybtn.hidden = true;
					kihumenu.innerHTML = "<a href='javascript:OnLinkClick(" + kihuindex + ")';>" + "棋譜: " + ary[1] + " -> " + ary[2] + "</a><br>" + kihumenu.innerHTML;
					kihuindex++;
					kihubtn.innerHTML = "棋譜終了"
					gamebtn.hidden = true;
				}
				break
			case "kihustone":
				stone(ary[1]);
				break;
		}
	}

	ws.onclose = function () {
		userlog.innerHTML = 'サーバーから切断されました<br>3秒後に再接続します。<br>'
		setTimeout(() => {
			connect();
		}, 3000);
	}

	ws.onerror = function () {
		userlog.innerHTML = 'サーバーの接続に失敗しました<br>';
	}
}

function stone(arystone) {
	resultbox.hidden = false;
	initStone();
	putcoord = [];
	var stone = arystone.split('');
	cntblack = 0;
	cntwhite = 0;
	var cntputblack = 0;
	var cntputwhite = 0;
	for (var i = 0; i < 8; i++) {
		for (var n = 0; n < 8; n++) {
			var index = (8 * i) + n;
			var type = Number(stone[index]);
			putStone(i, n, type);
			switch (type) {
				case 1:
					cntwhite++;
					break;
				case 2:
					cntblack++;
					break;
				case 3:
					putcoord.push([i, n]);
					cntputblack++;
					break;
				case 4:
					putcoord.push([i, n]);
					cntputwhite++;
					break;
			}
		}
	}
	if (cntblack + cntwhite == 0) {
		return;
	}
	if (kihumenu.hidden == true) {
		var cntput = cntputblack + cntputwhite;
		gameturn = (cntputblack > cntputwhite ? true : false) == turn ? true : false;
		var turnmess = gameturn ? "あなた" : "相手";
		result.innerHTML = turnmess + "のターン";
		if (cntput == 0) {
			if (cntblack + cntwhite != 64) {
				gameturn = true;
				result.innerHTML = "パス";
				if(gameturn){
					result.innerHTML += "<br>パスするには画面を押してください";
				}
				
			} else {
				winresult();
			}
		}
	}

	black.innerHTML = "黒:" + cntblack;
	white.innerHTML = "白:" + cntwhite;
}


function winresult() {
	var jodge = (cntblack > cntwhite) == turn ? 0 : 1;
	jodge = cntblack == cntwhite ? 3 : (cntblack > cntwhite) == turn ? 0 : 1;
	resmess = jodge == 0 ? "あなたの勝ち" : jodge == 1 ? "あなたの負け" : "引き分け";
	result.innerHTML = resmess;
	game = false;
	return jodge;
}

var canvas;
var context;
var block = 300 / 8;
var size;
function screenset() {
	changesize();
	canvas = document.getElementById('othello');
	context = canvas.getContext('2d');

	canvas.addEventListener("click", e => {
		const rect = e.target.getBoundingClientRect();

		// ブラウザ上での座標を求める
		const viewX = e.clientX - rect.left,
			viewY = e.clientY - rect.top;

		// 表示サイズとキャンバスの実サイズの比率を求める
		const scaleWidth = canvas.clientWidth / canvas.width * size,
			scaleHeight = canvas.clientHeight / canvas.height * size;

		// ブラウザ上でのクリック座標をキャンバス上に変換
		const canvasX = Math.floor(viewX / scaleWidth),
			canvasY = Math.floor(viewY / scaleHeight);

		var x = Math.floor(canvasX / block);
		var y = Math.floor(canvasY / block);

		stoneClick(x, y);
	});
}

window.addEventListener("resize", function () {
	if (wsize != window.innerWidth) {
		screenset();
	}
});

document.addEventListener('touchstart', (event) => {
	if (event.touches && event.touches.length > 1) {
		event.preventDefault();
	}
}, {
	passive: false
});

function changesize() {
	var main = document.getElementById('main');
	wsize = document.body.clientWidth;
	var hsize = window.innerHeight;
	if (wsize < hsize) {
		size = wsize / 300;
	} else {
		size = hsize / 500;
	}
	main.style.transform = "scale(" + size + ")";

}

function initStone() {
	context.beginPath();
	context.fillStyle = 'green';
	context.fillRect(0, 0, 300, 300);
	context.strokeStyle = 'black';
	context.lineWidth = 1;
	for (var i = 0; i < 9; i++) {
		var move = i % 2 == 0 ? block * i - 0.5 : block * i;
		context.moveTo(move, 0);
		context.lineTo(move, 300);
		context.moveTo(0, move);
		context.lineTo(300, move);
	}
	context.stroke();
	var position = [[block * 2 - 0.5, block * 2 - 0.5], [block * 6 - 0.5, block * 2 - 0.5], [block * 2 - 0.5, block * 6 - 0.5], [block * 6 - 0.5, block * 6 - 0.5]];
	context.fillStyle = 'black';
	for (var index in position) {
		context.beginPath();
		context.arc(position[index][0], position[index][1], Math.PI * 1, Math.PI * 360, false);
		context.fill();
	}
}

function putStone(x, y, type) {
	context.beginPath();
	if ((turn && type == 4) || (!turn && type == 3)) {
		if(kihumenu.hidden == true){
			return;
		}
	}
	var stonex = block * x;
	var stoney = block * y;
	context.arc(stonex + block / 2, stoney + block / 2, 15, Math.PI * 1, Math.PI * 360, false);
	switch (type) {
		case 0:
			return false;
		case 1:
			context.fillStyle = 'white';
			context.fill();
			break;
		case 2:
			context.fillStyle = 'black';
			context.fill();
			break;
		case 3:
			context.strokeStyle = 'black';
			context.lineWidth = 2;
			context.stroke();
			break;
		case 4:
			context.strokeStyle = 'white';
			context.lineWidth = 2;
			context.stroke();
			break;
		case 5:
			if (ainavi) {
				break;
			}
			context.strokeStyle = 'pink';
			context.lineWidth = 2.2;
			context.stroke();
			break;
		case 6:
			context.strokeStyle = 'red';
			context.lineWidth = 2;
			context.stroke();
			break;
		default:
			break;
	}
}
function putEva(x, y, type, eva) {
	context.beginPath();
	if (ainavi) {
		return;
	}
	var color = type == 3 ? "black" : "white";
	var stonex = block * x;
	var stoney = block * y;
	var fontpositionx = (block * 0.4375) - (block * 0.0625) * eva.length;
	var fontpositiony = (block * 0.625);
	context.fillStyle = color;
	context.font = "12px serif";
	context.fillText(eva, stonex + fontpositionx, stoney + fontpositiony);


}