//var ws = new WebSocket('ws://lalalasyun.com/websocket/othello');
var ws = new WebSocket('wss://othellojp.herokuapp.com/othello');

var turn;
var gameturn = true;
var game = false;
var cntblack = 0;
var cntwhite = 0;
var user1;
var user2;
var formIsEmpty;
//document.getElementById
var result;
var black;
var white;
var start;
var logout;
var userlog;
var userid;
var userpass;
var form;
var userdata;
var username1;
var username2;
var userrate1;
var userrate2;

function load() {
	result = document.getElementById('result');
	black = document.getElementById("black");
	white = document.getElementById("white");
	start = document.getElementById('startid');
	logout = document.getElementById('logout');
	userlog = document.getElementById('usermenu');
	userid = document.getElementById("id");
	userpass = document.getElementById("pass");
	form = document.getElementById('form');
	username1 = document.getElementById('username1');
	username2 = document.getElementById('username2');
	userrate1 = document.getElementById('userrate1');
	userrate2 = document.getElementById('userrate2');
	userdata = document.getElementById('userdata');

}

function stoneClick(x, y) {
	if (game && gameturn) {
		ws.send("coord," + x + "," + y);
	}
}

function startbtn() {
	ws.send("start");
}

function online() {
	if (game) { return; }


	if (!start.disabled) {
		result.innerHTML = "プレイヤーを待っています";
		start.disabled = true;
		ws.send("online");
		userdata.hidden = true;
		black.innerHTML = "";
		white.innerHTML = "";
	} else {
		userdata.hidden = false;
		result.innerHTML = "";
		start.disabled = false;
		ws.send("offline");
	}
}

function login() {
	if (game) {
		return;
	}
	userlog.innerHTML = "";
	userid.innerHTML = "";
	userpass.innerHTML = "";
	form.hidden = form.hidden ? false : true;
}

function loginbtn() {
	if(formIsEmpty){
		ws.send("login," + userid.value + "," + userpass.value);
		form.hidden = true;
		return;
	}
	alert("フォームを入力してください");
}

function logoutbtn(){
	ws.send("logout");
	form.hidden = true;
	return;
}

function registerbtn() {
	if(formIsEmpty){
		ws.send("register," + userid.value + "," + userpass.value);
		form.hidden = true;
		return;
	}
	alert("フォームを入力してください");
}

function deletebtn(){
	if(formIsEmpty){
		ws.send("delete," + userid.value + "," + userpass.value);
		form.hidden = true;
		return;
	}
	alert("フォームを入力してください");
}

function inputChange(){
	formIsEmpty = userid.value == "" && userpass.value == "" ? false:true;
	logout.disabled = formIsEmpty;
}

function record() {

}

ws.onmessage = function (receive) {
	var ary = receive.data.split(',');
	var command = ary[0];
	var log =['ログイン','ログアウト','登録','削除'];
	var mess =['しました','に失敗しました'];
	switch (command) {
		case "stone":
			initStone();
			var stone = ary[1].split('');
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
							cntputblack++;
							break;
						case 4:
							cntputwhite++;
							break;
					}
				}
			}
			var cntput = cntputblack + cntputwhite;
			gameturn = (cntputblack > cntputwhite ? "black" : "white") == turn ? true : false;
			var turnmess = gameturn ? "あなた" : "相手";
			result.innerHTML = turnmess + "のターン";
			if (cntput == 0) {
				gameturn = true;
				result.innerHTML = "パス";
			}
			black.innerHTML = "黒:" + cntblack;
			white.innerHTML = "白:" + cntwhite;
			break;
		case "turn":
			turn = ary[1];
			break;
		case "matching":
			user1 = ary[2];
			user2 = ary[3];
			username1.innerHTML = user1;
			username2.innerHTML = user2;
			userdata.hidden = false;
			if (!game && ary[3]!="AI") {
				start.disabled = false;
				game = true;
				result.innerHTML = "プレイヤーが見つかりました";
			}
			break;
		case "rate":
			userrate2.innerHTML = ary[1];
			break;
		case "login":
			var index = Number(ary[1]);
			userlog.innerHTML = ary[2] == "success" ? log[index]+mess[0]:log[index]+mess[1];
			break;
		case "start":
			result.innerHTML = "";
			black.innerHTML = "";
			white.innerHTML = "";
			userlog.innerHTML = "";
			if (game) {
				game = false;
				start.innerHTML = "スタート";
			}else{
				game = true;
				start.innerHTML = "リセット";
			}
			initStone();
			break;
		case "end":
			winresult();
			start.innerHTML = "スタート";
			break;
	}
}

function winresult() {
	var resmess = cntblack > cntwhite ? "黒の勝ち" : "白の勝ち";
	var resmess = cntblack == cntwhite ? "引き分け" : resmess;
	result.innerHTML = resmess;
	game = false;
}

var canvas;
var context;
var block = 300 / 8;
var size;
function sample() {
	canvas = document.getElementById('othello');
	changesize();

	if (canvas.getContext) {
		context = canvas.getContext('2d');
		initStone();
	}

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
	load();
}

function changesize() {
	var main = document.getElementById('main');
	var wsize = screen.width;
	size = wsize / 300;
	if (size < 2) {
		main.style.transform = "scale(" + size + ")";
	} else {
		size = 2;
		main.style.transform = "scale(" + size + ")";
	}
}

function initStone() {
	context.beginPath();
	context.fillStyle = 'green';
	context.fillRect(0, 0, 300, 300);

	for (var i = 0; i < 9; i++) {
		var move = block * i;
		context.strokeStyle = 'black';
		context.lineWidth = 2;

		context.moveTo(move, 0);
		context.lineTo(move, 300);
		context.stroke();

		context.moveTo(0, move);
		context.lineTo(300, move);
		context.stroke();
	}
}

function putStone(x, y, type) {
	context.beginPath();
	if (turn == "black" && type == 4) {
		return;
	} else if (turn == "white" && type == 3) {
		return;
	}
	var stonex = block * x;
	var stoney = block * y;
	context.arc(stonex + block / 2, stoney + block / 2, 15, 0 * Math.PI / 180, 360 * Math.PI / 180, false);
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
		default:
			return false;
	}

}