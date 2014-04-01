package com.akkuma.kitinfo;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JButton;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JLabel;
import javax.swing.JTextArea;

import com.akkuma.kitinfo.core.KITInfo;
import com.akkuma.kitinfo.core.KITInfo.DebugOutputListener;
import com.akkuma.kitinfo.util.FileUtils;

import javax.swing.JTextField;
import javax.swing.JPasswordField;
import javax.swing.JCheckBox;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.UIManager;

@SuppressWarnings({ "serial" })
public class MainFrame extends JFrame implements DebugOutputListener {

    private static final String SETTINGS_LOG_FILE = "settings.log";

    private JPanel contentPane;
    private KITInfo mKitInfo;
    private KITInfoThread mKitInfoThread;
    private Timer mTimer;
    private final SimpleDateFormat mStateDateFormat = new SimpleDateFormat("次の実行はyyyy/MM/dd HH:mm:ss:SSS");

    private class KITInfoThread extends Thread {

        private boolean parse;
        private boolean weather;
        private boolean dayTweet;

        public KITInfoThread(boolean parse, boolean weather, boolean dayTweet) {
            this.parse = parse;
            this.weather = weather;
            this.dayTweet = dayTweet;
        }

        @SuppressWarnings("deprecation")
        @Override
        public void run() {
            mKitInfo = new KITInfo();
            mKitInfo.setDebugOutputListener(MainFrame.this);
            if (parse) {
                mKitInfo.start(mPortalIdTextField.getText(), mPortalPasswordField.getText());
            }
            if (weather) {
                mKitInfo.weather(mWeatherRssUrlTextField.getText(), mProxyHostTextField.getText(), Integer.parseInt(mProxyPortTextField.getText()));
            }
            if (dayTweet) {
                mKitInfo.dayTweet();
            }
            mKitInfo.tweetQueue(mDisableTweetCheckBox.isSelected(), mConsumerKeyTextField.getText(), mConsumerSecretTextField.getText(), mAccessTokenTextField.getText(),
                    mAccessTokenSecretTextField.getText(), mProxyHostTextField.getText(), Integer.parseInt(mProxyPortTextField.getText()));
            mKitInfo.destroy();
            mForceStartButton.setEnabled(true);
        };
    }

    private SettingsContainer mSettingsContainer;

    private class TimerTaskImpl extends TimerTask {

        @Override
        public void run() {
            Calendar nextExecute = Calendar.getInstance();
            int minute = nextExecute.get(Calendar.MINUTE);
            int hour = nextExecute.get(Calendar.HOUR_OF_DAY);
            nextExecute.add(Calendar.MINUTE, 15 - (minute % 15));
            nextExecute.set(Calendar.SECOND, 0);
            nextExecute.set(Calendar.MILLISECOND, 0);
            mTimer = new Timer();
            mTimer.schedule(new TimerTaskImpl(), nextExecute.getTime());
            mStateLabel.setText(mStateDateFormat.format(nextExecute.getTime()));
            mForceStartButton.setEnabled(false);
            mTextArea.setText("");
            boolean parse = minute == 0;
            boolean weather = hour == (int) mWeatherTweetHourSpinner.getValue() && minute == (int) mWeatherTweetMinuteSpinner.getValue();
            boolean dayTweet = hour == (int) mDayTweetTimeHourSpinner.getValue() && minute == (int) mDayTweetTimeMinuteSpinner.getValue();
            mKitInfoThread = new KITInfoThread(parse, weather, dayTweet);
            mKitInfoThread.start();
        }
    };

    private final ActionListener mStartButtonActionListener = new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent e) {
            Calendar nextExecute = Calendar.getInstance();
            int minute = nextExecute.get(Calendar.MINUTE);
            nextExecute.add(Calendar.MINUTE, 15 - (minute % 15));
            nextExecute.set(Calendar.SECOND, 0);
            nextExecute.set(Calendar.MILLISECOND, 0);

            mTimer = new Timer();
            mTimer.schedule(new TimerTaskImpl(), nextExecute.getTime());
            mStateLabel.setText(mStateDateFormat.format(nextExecute.getTime()));

            mStartButton.setEnabled(false);
        }
    };
    private final ActionListener mStopButtonActionListener = new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                if (mKitInfoThread != null) {
                    mKitInfoThread.interrupt();
                }
                if (mTimer != null) {
                    mTimer.cancel();
                }
                mStartButton.setEnabled(true);
                mForceStartButton.setEnabled(true);
                mStateLabel.setText("開始していません。");
            } catch (SecurityException e1) {
            }
        }
    };
    private final ActionListener mForceStartButtonActionListener = new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent e) {
            mForceStartButton.setEnabled(false);
            mTextArea.setText("");
            mKitInfoThread = new KITInfoThread(true, false, true);
            mKitInfoThread.start();
        }
    };

    private final WindowAdapter mWindowAdapter = new WindowAdapter() {

        @SuppressWarnings("deprecation")
        @Override
        public void windowClosing(java.awt.event.WindowEvent e) {
            mStopButton.doClick();
            mSettingsContainer.setPortalId(mPortalIdTextField.getText());
            mSettingsContainer.setPortalPassword(mPortalPasswordField.getText());
            mSettingsContainer.setConsumerKey(mConsumerKeyTextField.getText());
            mSettingsContainer.setConsumerSecret(mConsumerSecretTextField.getText());
            mSettingsContainer.setAccessToken(mAccessTokenTextField.getText());
            mSettingsContainer.setAccessTokenSecret(mAccessTokenSecretTextField.getText());
            mSettingsContainer.setProxyHost(mProxyHostTextField.getText());
            mSettingsContainer.setProxyPort(mProxyPortTextField.getText());
            mSettingsContainer.setDisableTweetCheck(mDisableTweetCheckBox.isSelected());
            mSettingsContainer.setWeatherRssUrl(mWeatherRssUrlTextField.getText());
            mSettingsContainer.setWeatherTweetHour((int) mWeatherTweetHourSpinner.getValue());
            mSettingsContainer.setWeatherTweetMinute((int) mWeatherTweetMinuteSpinner.getValue());
            mSettingsContainer.setDayTweetTimeHour((int) mDayTweetTimeHourSpinner.getValue());
            mSettingsContainer.setDayTweetTimeMinute((int) mDayTweetTimeMinuteSpinner.getValue());
            FileUtils.writeObjectToFile(mSettingsContainer, SETTINGS_LOG_FILE);
        };
    };

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                UIManager.put("swing.boldMetal", Boolean.FALSE);
                try {
                    MainFrame frame = new MainFrame();
                    frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private JButton mStartButton;
    private JTextField mPortalIdTextField;
    private JPasswordField mPortalPasswordField;
    private JTextField mConsumerKeyTextField;
    private JTextField mConsumerSecretTextField;
    private JTextField mAccessTokenTextField;
    private JTextField mAccessTokenSecretTextField;
    private JButton mStopButton;
    private JButton mForceStartButton;
    private JTextField mProxyPortTextField;
    private JTextField mProxyHostTextField;
    private JCheckBox mDisableTweetCheckBox;
    private JLabel mStateLabel;
    private JTextArea mTextArea;
    private JTextField mWeatherRssUrlTextField;
    private JSpinner mWeatherTweetHourSpinner;
    private JSpinner mWeatherTweetMinuteSpinner;
    private JSpinner mDayTweetTimeHourSpinner;
    private JSpinner mDayTweetTimeMinuteSpinner;
    private JCheckBox mDontSaveCheckBox;

    /**
     * Create the frame.
     */
    public MainFrame() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        addWindowListener(mWindowAdapter);
        setTitle("KIT_info Manager");
        setBounds(100, 100, 560, 590);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);

        mStartButton = new JButton("Start");
        mStartButton.setBounds(5, 5, 91, 21);
        mStartButton.addActionListener(mStartButtonActionListener);
        contentPane.setLayout(null);
        contentPane.add(mStartButton);

        mStopButton = new JButton("Stop");
        mStopButton.setBounds(102, 5, 91, 21);
        mStopButton.addActionListener(mStopButtonActionListener);
        contentPane.add(mStopButton);

        mForceStartButton = new JButton("強制的に開始");
        mForceStartButton.setBounds(5, 28, 188, 21);
        mForceStartButton.addActionListener(mForceStartButtonActionListener);
        contentPane.add(mForceStartButton);

        mStateLabel = new JLabel("開始していません。");
        mStateLabel.setVerticalAlignment(SwingConstants.TOP);
        mStateLabel.setBounds(5, 113, 188, 36);
        contentPane.add(mStateLabel);

        JLabel label = new JLabel("学籍番号");
        label.setBounds(205, 9, 73, 13);
        contentPane.add(label);

        mPortalIdTextField = new JTextField();
        mPortalIdTextField.setBounds(290, 6, 112, 19);
        contentPane.add(mPortalIdTextField);
        mPortalIdTextField.setColumns(10);

        mPortalPasswordField = new JPasswordField();
        mPortalPasswordField.setBounds(290, 35, 112, 19);
        contentPane.add(mPortalPasswordField);

        JLabel label_1 = new JLabel("パスワード");
        label_1.setBounds(205, 38, 73, 13);
        contentPane.add(label_1);

        JLabel lblNewLabel = new JLabel("Consumer Key");
        lblNewLabel.setBounds(205, 64, 137, 13);
        contentPane.add(lblNewLabel);

        JLabel lblNewLabel_1 = new JLabel("Consumer Secret");
        lblNewLabel_1.setBounds(205, 87, 137, 13);
        contentPane.add(lblNewLabel_1);

        JLabel lblAccessToken = new JLabel("Access Token");
        lblAccessToken.setBounds(205, 110, 137, 13);
        contentPane.add(lblAccessToken);

        JLabel lblAccessTokenSecret = new JLabel("Access Token Secret");
        lblAccessTokenSecret.setBounds(205, 133, 137, 13);
        contentPane.add(lblAccessTokenSecret);

        mConsumerKeyTextField = new JTextField();
        mConsumerKeyTextField.setBounds(354, 61, 178, 19);
        contentPane.add(mConsumerKeyTextField);
        mConsumerKeyTextField.setColumns(10);

        mConsumerSecretTextField = new JTextField();
        mConsumerSecretTextField.setBounds(354, 84, 178, 19);
        contentPane.add(mConsumerSecretTextField);
        mConsumerSecretTextField.setColumns(10);

        mAccessTokenTextField = new JTextField();
        mAccessTokenTextField.setBounds(354, 107, 178, 19);
        contentPane.add(mAccessTokenTextField);
        mAccessTokenTextField.setColumns(10);

        mAccessTokenSecretTextField = new JTextField();
        mAccessTokenSecretTextField.setBounds(354, 130, 178, 19);
        contentPane.add(mAccessTokenSecretTextField);
        mAccessTokenSecretTextField.setColumns(10);

        JLabel lblProxyHost = new JLabel("Proxy Host");
        lblProxyHost.setBounds(205, 156, 137, 13);
        contentPane.add(lblProxyHost);

        mProxyHostTextField = new JTextField();
        mProxyHostTextField.setBounds(354, 153, 178, 19);
        contentPane.add(mProxyHostTextField);
        mProxyHostTextField.setColumns(10);

        JLabel lblProxyPort = new JLabel("Proxy Port");
        lblProxyPort.setBounds(205, 179, 137, 13);
        contentPane.add(lblProxyPort);

        mProxyPortTextField = new JTextField();
        mProxyPortTextField.setBounds(354, 176, 178, 19);
        contentPane.add(mProxyPortTextField);
        mProxyPortTextField.setColumns(10);

        mDisableTweetCheckBox = new JCheckBox("ツイートを無効化");
        mDisableTweetCheckBox.setBounds(5, 55, 188, 21);
        contentPane.add(mDisableTweetCheckBox);

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBounds(5, 271, 527, 270);
        contentPane.add(scrollPane);

        mTextArea = new JTextArea();
        mTextArea.setEditable(false);
        scrollPane.setViewportView(mTextArea);

        JLabel lblWeatherRssUrl = new JLabel("Weather RSS URL");
        lblWeatherRssUrl.setBounds(205, 202, 137, 13);
        contentPane.add(lblWeatherRssUrl);

        mWeatherRssUrlTextField = new JTextField();
        mWeatherRssUrlTextField.setBounds(354, 199, 178, 19);
        contentPane.add(mWeatherRssUrlTextField);
        mWeatherRssUrlTextField.setColumns(10);

        JLabel lblWeatherTweetTime = new JLabel("Weather Tweet Time");
        lblWeatherTweetTime.setBounds(205, 225, 137, 13);
        contentPane.add(lblWeatherTweetTime);

        mWeatherTweetHourSpinner = new JSpinner();
        mWeatherTweetHourSpinner.setModel(new SpinnerNumberModel(0, 0, 23, 1));
        mWeatherTweetHourSpinner.setBounds(354, 222, 48, 20);
        contentPane.add(mWeatherTweetHourSpinner);

        JLabel label_2 = new JLabel("時");
        label_2.setBounds(414, 225, 20, 13);
        contentPane.add(label_2);

        mWeatherTweetMinuteSpinner = new JSpinner();
        mWeatherTweetMinuteSpinner.setModel(new SpinnerNumberModel(0, 0, 45, 15));
        mWeatherTweetMinuteSpinner.setBounds(434, 222, 48, 20);
        contentPane.add(mWeatherTweetMinuteSpinner);

        JLabel label_3 = new JLabel("分");
        label_3.setBounds(494, 225, 26, 13);
        contentPane.add(label_3);

        JLabel lblDayTweetTime = new JLabel("Day Tweet Time");
        lblDayTweetTime.setBounds(205, 248, 137, 13);
        contentPane.add(lblDayTweetTime);

        mDayTweetTimeHourSpinner = new JSpinner();
        mDayTweetTimeHourSpinner.setModel(new SpinnerNumberModel(0, 0, 23, 1));
        mDayTweetTimeHourSpinner.setBounds(354, 245, 48, 20);
        contentPane.add(mDayTweetTimeHourSpinner);

        JLabel label_4 = new JLabel("時");
        label_4.setBounds(414, 248, 20, 13);
        contentPane.add(label_4);

        mDayTweetTimeMinuteSpinner = new JSpinner();
        mDayTweetTimeMinuteSpinner.setModel(new SpinnerNumberModel(0, 0, 45, 15));
        mDayTweetTimeMinuteSpinner.setBounds(434, 245, 48, 20);
        contentPane.add(mDayTweetTimeMinuteSpinner);

        JLabel label_5 = new JLabel("分");
        label_5.setBounds(494, 248, 26, 13);
        contentPane.add(label_5);

        mDontSaveCheckBox = new JCheckBox("ログをセーブしない");
        mDontSaveCheckBox.setBounds(5, 79, 188, 21);
        mDontSaveCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FileUtils.setDontSave(mDontSaveCheckBox.isSelected());
            }
        });

        mSettingsContainer = (SettingsContainer) FileUtils.readObjectFromFile(SETTINGS_LOG_FILE);
        if (mSettingsContainer == null) {
            mSettingsContainer = new SettingsContainer();
        }

        mPortalIdTextField.setText(mSettingsContainer.getPortalId());
        mPortalPasswordField.setText(mSettingsContainer.getPortalPassword());
        mConsumerKeyTextField.setText(mSettingsContainer.getConsumerKey());
        mConsumerSecretTextField.setText(mSettingsContainer.getConsumerSecret());
        mAccessTokenTextField.setText(mSettingsContainer.getAccessToken());
        mAccessTokenSecretTextField.setText(mSettingsContainer.getAccessTokenSecret());
        mProxyHostTextField.setText(mSettingsContainer.getProxyHost());
        mProxyPortTextField.setText(mSettingsContainer.getProxyPort());
        mDisableTweetCheckBox.setSelected(mSettingsContainer.isDisableTweetCheck());
        mWeatherRssUrlTextField.setText(mSettingsContainer.getWeatherRssUrl());
        mWeatherTweetHourSpinner.setValue(mSettingsContainer.getWeatherTweetHour());
        mWeatherTweetMinuteSpinner.setValue(mSettingsContainer.getWeatherTweetMinute());
        mDayTweetTimeHourSpinner.setValue(mSettingsContainer.getDayTweetTimeHour());
        mDayTweetTimeMinuteSpinner.setValue(mSettingsContainer.getDayTweetTimeMinute());

        contentPane.add(mDontSaveCheckBox);

        mStartButton.doClick();
    }

    @Override
    public void onOutput(String text) {
        mTextArea.append(text + "\n");
    }
}
