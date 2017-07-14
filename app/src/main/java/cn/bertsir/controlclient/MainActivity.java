package cn.bertsir.controlclient;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class MainActivity extends Activity implements View.OnClickListener {

    private EditText et_shell;
    private Button bt_send;
    private PrintWriter write;
    private BufferedReader in;
    private Socket socket;
    private Button bt_back;
    private Button bt_home;
    private Button bt_power;
    private Button bt_volumeadd;
    private Button bt_volumejian;
    private FrameLayout fl_root;


    private float upX;
    private float downY;
    private float downX;
    private float upY;
    private ImageView iv_screen;
    private Button bt_get_connect;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:

                    break;
            }
        }
    };
    private EditText et_ip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();

    }

    private void initView() {
        et_shell = (EditText) findViewById(R.id.et_shell);
        bt_send = (Button) findViewById(R.id.bt_send);
        bt_send.setOnClickListener(this);
        bt_back = (Button) findViewById(R.id.bt_back);
        bt_back.setOnClickListener(this);
        bt_home = (Button) findViewById(R.id.bt_home);
        bt_home.setOnClickListener(this);
        bt_power = (Button) findViewById(R.id.bt_power);
        bt_power.setOnClickListener(this);
        bt_volumeadd = (Button) findViewById(R.id.bt_volumeadd);
        bt_volumeadd.setOnClickListener(this);
        bt_volumejian = (Button) findViewById(R.id.bt_volumejian);
        bt_volumejian.setOnClickListener(this);
        fl_root = (FrameLayout) findViewById(R.id.fl_root);
        setListener();
        iv_screen = (ImageView) findViewById(R.id.iv_screen);
        bt_get_connect = (Button) findViewById(R.id.bt_get_connect);
        bt_get_connect.setOnClickListener(this);
        et_ip = (EditText) findViewById(R.id.et_ip);
        et_ip.setOnClickListener(this);
    }

    private void setListener() {
        fl_root.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    downX = event.getX();
                    downY = event.getY();
                }

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    upX = event.getX();
                    upY = event.getY();

                    if (Math.abs(upX - downX) < 20 || Math.abs(upY - downY) < 20) {
                        sendDate("input tap " + (int) upX + " " + (int) upY);
                    } else {
                        sendDate("input swipe " + (int) downX + " " + (int) downY + " " + (int) upX + " " + (int) upY);
                    }
                }

                return false;
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_send:
                String shell = et_shell.getText().toString();
                sendDate(shell);
                break;
            case R.id.bt_back:
                sendDate("input keyevent KEYCODE_BACK");
                break;
            case R.id.bt_home:
                sendDate("input keyevent KEYCODE_HOME");
                break;
            case R.id.bt_power:
                sendDate("input keyevent KEYCODE_POWER");
                break;
            case R.id.bt_volumeadd:
                sendDate("input keyevent KEYCODE_VOLUME_UP");
                break;
            case R.id.bt_volumejian:
                sendDate("input keyevent KEYCODE_VOLUME_DOWN");
                break;
            case R.id.bt_get_connect:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        connectSocket(et_ip.getText().toString().trim());
                    }
                }).start();
                break;
        }
    }

    public void connectSocket(String ipAndPort) {
        try {
            String[] split = ipAndPort.split(":");
            socket = new Socket(split[0], Integer.parseInt(split[1]));
            System.out.println("客户端启动成功");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "客户端启动成功", Toast.LENGTH_SHORT).show();
                }
            });

            write = new PrintWriter(socket.getOutputStream());
            DataInputStream dataInput = new DataInputStream(socket.getInputStream());
            while (true) {
                int size = dataInput.readInt();
                byte[] data = new byte[size];
                int len = 0;
                while (len < size) {
                    len += dataInput.read(data, len, size - len);
                }
                ByteArrayOutputStream outPut = new ByteArrayOutputStream();
                final Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                bmp.compress(Bitmap.CompressFormat.JPEG, 80, outPut);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        iv_screen.setImageBitmap(bmp);
                    }
                });

            }

        } catch (Exception e) {
            System.out.println("can not listen to:" + e);// 出错，打印出错信息
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "客户端连接失败", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void sendDate(String exe) {
        write.println(exe);
        write.flush();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            stopConnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopConnect() throws IOException {
        write.close(); // 关闭Socket输出流
        in.close(); // 关闭Socket输入流
        socket.close(); // 关闭Socket
    }

    private void submit() {
        // validate
        String ip = et_ip.getText().toString().trim();
        if (TextUtils.isEmpty(ip)) {
            Toast.makeText(this, "ip不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        // TODO validate success, do something


    }
}
