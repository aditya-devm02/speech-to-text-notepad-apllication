package com.mycompany.helloapp;

import java.io.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.sound.sampled.*;
import javax.swing.undo.*;
import javax.swing.event.UndoableEditListener;
import javax.swing.event.UndoableEditEvent;
import okhttp3.*;
import org.json.*;

public class NotePad extends JFrame implements ActionListener, WindowListener {
    JTextArea jta = new JTextArea();
    File fnameContainer;
    UndoManager undoManager = new UndoManager();

    public NotePad() {
        Font fnt = new Font("Arial", Font.PLAIN, 15);
        Container con = getContentPane();

        JMenuBar jmb = new JMenuBar();
        JMenu jmfile = new JMenu("File");
        JMenu jmedit = new JMenu("Edit");
        JMenu jmhelp = new JMenu("About");

        con.setLayout(new BorderLayout());

        JScrollPane sbrText = new JScrollPane(jta);
        sbrText.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        jta.setFont(fnt);
        jta.setLineWrap(true);
        jta.setWrapStyleWord(true);

        con.add(sbrText);

        createMenuItem(jmfile, "New");
        createMenuItem(jmfile, "Open");
        createMenuItem(jmfile, "Save");
        jmfile.addSeparator();
        createMenuItem(jmfile, "Exit");

        createMenuItem(jmedit, "Cut");
        createMenuItem(jmedit, "Copy");
        createMenuItem(jmedit, "Paste");
        createMenuItem(jmedit, "Undo");
        createMenuItem(jmedit, "Redo");
        createMenuItem(jmedit, "Select All");
        createMenuItem(jmedit, "Search");
        createMenuItem(jmedit, "Replace");
        createMenuItem(jmedit, "Voice Typing");

        createMenuItem(jmhelp, "About Notepad");

        jmb.add(jmfile);
        jmb.add(jmedit);
        jmb.add(jmhelp);

        setJMenuBar(jmb);
        setIconImage(Toolkit.getDefaultToolkit().getImage("notepad.gif"));
        addWindowListener(this);
        setSize(500, 500);
        setTitle("Untitled.txt - Notepad");

        setVisible(true);

        jta.getDocument().addUndoableEditListener(new UndoableEditListener() {
            public void undoableEditHappened(UndoableEditEvent e) {
                undoManager.addEdit(e.getEdit());
            }
        });
    }

    public void createMenuItem(JMenu jm, String txt) {
        JMenuItem jmi = new JMenuItem(txt);
        jmi.addActionListener(this);
        jm.add(jmi);
    }

    public void actionPerformed(ActionEvent e) {
        JFileChooser jfc = new JFileChooser();

        if (e.getActionCommand().equals("New")) {
            this.setTitle("Untitled.txt - Notepad");
            jta.setText("");
            fnameContainer = null;
        } else if (e.getActionCommand().equals("Open")) {
            int ret = jfc.showDialog(null, "Open");

            if (ret == JFileChooser.APPROVE_OPTION) {
                try {
                    File fyl = jfc.getSelectedFile();
                    OpenFile(fyl.getAbsolutePath());
                    this.setTitle(fyl.getName() + " - Notepad");
                    fnameContainer = fyl;
                } catch (IOException ers) {
                    JOptionPane.showMessageDialog(this, "Unable to open file", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }

        } else if (e.getActionCommand().equals("Save")) {
            if (fnameContainer != null) {
                jfc.setCurrentDirectory(fnameContainer);
                jfc.setSelectedFile(fnameContainer);
            } else {
                jfc.setSelectedFile(new File("Untitled.txt"));
            }

            int ret = jfc.showSaveDialog(null);

            if (ret == JFileChooser.APPROVE_OPTION) {
                try {
                    File fyl = jfc.getSelectedFile();
                    SaveFile(fyl.getAbsolutePath());
                    this.setTitle(fyl.getName() + " - Notepad");
                    fnameContainer = fyl;
                } catch (Exception ers2) {
                    JOptionPane.showMessageDialog(this, "Unable to save file", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }

        } else if (e.getActionCommand().equals("Exit")) {
            Exiting();
        } else if (e.getActionCommand().equals("Copy")) {
            jta.copy();
        } else if (e.getActionCommand().equals("Paste")) {
            jta.paste();
        } else if (e.getActionCommand().equals("About Notepad")) {
            JOptionPane.showMessageDialog(this, "Created by: Aditya and Vigyat", "Notepad", JOptionPane.INFORMATION_MESSAGE);
        } else if (e.getActionCommand().equals("Cut")) {
            jta.cut();
        } else if (e.getActionCommand().equals("Undo")) {
            if (undoManager.canUndo()) {
                undoManager.undo();
            }
        } else if (e.getActionCommand().equals("Redo")) {
            if (undoManager.canRedo()) {
                undoManager.redo();
            }
        } else if (e.getActionCommand().equals("Select All")) {
            jta.selectAll();
        } else if (e.getActionCommand().equals("Search")) {
            String searchWord = JOptionPane.showInputDialog(this, "Enter word to search:");
            if (searchWord != null) {
                searchWordInText(searchWord);
            }
        } else if (e.getActionCommand().equals("Replace")) {
            String searchWord = JOptionPane.showInputDialog(this, "Enter word to replace:");
            if (searchWord != null) {
                String replaceWord = JOptionPane.showInputDialog(this, "Enter replacement word:");
                if (replaceWord != null) {
                    replaceWordInText(searchWord, replaceWord);
                }
            }
        } else if (e.getActionCommand().equals("Voice Typing")) {
            try {
                voiceTyping();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error with voice typing: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void searchWordInText(String word) {
        String text = jta.getText();
        int index = text.indexOf(word);
        if (index >= 0) {
            jta.setCaretPosition(index);
            jta.setSelectionStart(index);
            jta.setSelectionEnd(index + word.length());
            jta.requestFocusInWindow();
        } else {
            JOptionPane.showMessageDialog(this, "Word not found", "Search", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public void replaceWordInText(String searchWord, String replaceWord) {
        String text = jta.getText();
        text = text.replaceAll(searchWord, replaceWord);
        jta.setText(text);
    }

    public void voiceTyping() throws Exception {
        // Record audio from the microphone
        File audioFile = new File("audio.wav");
        AudioFormat format = new AudioFormat(16000, 16, 1, true, true);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
        if (!AudioSystem.isLineSupported(info)) {
            throw new LineUnavailableException("Line not supported");
        }
        TargetDataLine line = (TargetDataLine) AudioSystem.getLine(info);
        line.open(format);
        line.start();
        AudioInputStream ais = new AudioInputStream(line);

        Thread stopper = new Thread(() -> {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            line.stop();
            line.close();
        });
        stopper.start();

        AudioSystem.write(ais, AudioFileFormat.Type.WAVE, audioFile);

        // Call OpenAI API to transcribe the audio
        OkHttpClient client = new OkHttpClient();
        String apiKey = "YOUR_OPENAI_API_KEY";

        MediaType mediaType = MediaType.parse("audio/wav");
        RequestBody body = RequestBody.create(mediaType, audioFile);
        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/audio/transcriptions")
                .post(body)
                .addHeader("Authorization", "Bearer " + apiKey)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

            JSONObject jsonResponse = new JSONObject(response.body().string());
            String transcript = jsonResponse.getString("text");
            jta.append(transcript + "\n");
        }
    }

    public void OpenFile(String fname) throws IOException {
        BufferedReader d = new BufferedReader(new InputStreamReader(new FileInputStream(fname)));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = d.readLine()) != null) {
            sb.append(line).append("\n");
        }
        jta.setText(sb.toString());
        d.close();
    }

    public void SaveFile(String fname) throws IOException {
        BufferedWriter out = new BufferedWriter(new FileWriter(fname));
        out.write(jta.getText());
        out.close();
    }

    public void Exiting() {
        System.exit(0);
    }

    public void windowDeactivated(WindowEvent e) {}

    public void windowActivated(WindowEvent e) {}

    public void windowDeiconified(WindowEvent e) {}

    public void windowIconified(WindowEvent e) {}

    public void windowClosed(WindowEvent e) {}

    public void windowClosing(WindowEvent e) {
        Exiting();
    }

    public void windowOpened(WindowEvent e) {}

    public static void main(String[] args) {
        new NotePad();
    }
}
