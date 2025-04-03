import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.*;
import java.util.regex.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.util.Map;
import java.util.LinkedHashMap;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.List;
import java.util.ArrayList;
public class codeEditor {
    public static Map<JPanel, String> map = new LinkedHashMap<>();
    public static Map<JPanel, List<String>> widgets = new LinkedHashMap<>();
    public static boolean updating = false;
    private static JPanel panelPaths;
    private static JPanel fenetres;
    private static JFrame frame;
    public static int nombreLignes = 10;
    private static JTextPane actualCode = new JTextPane();
    public static Thread read;
    private static final Object lock = new Object();
    public static List<List<Integer>> colorRouge = new ArrayList<>();
    public static List<List<Integer>> colorJaune = new ArrayList<>();
    public static Boolean requestFocus = false;
    public static Boolean createFile = false;
    public static Boolean repeatRequest = true;
    public static codeEditor terminal;
    public static String command = ""; 
    public static String addedText = "";
    static {
        System.loadLibrary("codeEditor"); 
    }
    public native void CreateTerminal();  
    public native void SendCommand(String command); 
    public native String ReadOutput(); 
    public native String ReadError(); 
    public native void CloseTerminal();
    private static void colorText() {
        SwingUtilities.invokeLater(() -> {
            doc.setCharacterAttributes(0, commandes.getText().length(), defStyle, true);
        });
        SwingUtilities.invokeLater(() -> {
            for (List<Integer> liste : colorJaune) {
                doc.setCharacterAttributes(liste.get(0), liste.get(1), input, true);
            }
        });
        SwingUtilities.invokeLater(() -> {
            for (List<Integer> liste : colorRouge) {
                doc.setCharacterAttributes(liste.get(0), liste.get(1), error, true);
            }
        });
    }
    private static void communicateWithCMD(String text) {
        synchronized (lock) {
            try {
                if (text.trim().equals("python")) {
                    text = text + " -i";
                    TextSauvegarde = commandes.getText() + " -i";
                } else if (text.trim().equals("node")) {
                    text = text + " -i";
                    TextSauvegarde = commandes.getText() + " -i";
                } else if (text.trim().equals("powershell")) {
                    text = text + ".exe -NoExit -Command \"chcp 850 | Out-Null\"";
                    TextSauvegarde = commandes.getText() + " -i";
                } else if (text.trim().equals("powershell.exe")) {
                    text = text + " -NoExit -Command \"chcp 850 | Out-Null\"";
                    TextSauvegarde = commandes.getText() + " -i";
                } else if (text.trim().equals("cmd")) {
                    text = text + ".exe /K chcp 850 >nul";
                    TextSauvegarde = commandes.getText() + " -i";
                } else if (text.trim().equals("cmd.exe")) {
                    text = text + " chcp 850 >nul";
                    TextSauvegarde = commandes.getText() + " -i";
                }
                command = text;
                terminal.SendCommand(text);
                if (createFile) {
                    Thread.sleep(100);
                    createFile = false;
                    listerFichiers();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } 
        }
    }
    public static void updateCMD(List<String> liste) {
        SwingUtilities.invokeLater(() -> {
            liste.set(0, liste.get(0).replace("\r", ""));
            if (liste.get(1).equals("output")) {
                textAjoute = "";
                commandes.getDocument().removeDocumentListener(DocListener);
                TextSauvegarde = commandes.getText() + liste.get(0);
                commandes.setText(commandes.getText() + liste.get(0));
                commandes.getDocument().addDocumentListener(DocListener);
            } else {
                commandes.getDocument().removeDocumentListener(DocListener);
                int length = commandes.getText().length();
                TextSauvegarde = commandes.getText() + liste.get(0);
                commandes.setText(commandes.getText() + liste.get(0));
                commandes.getDocument().addDocumentListener(DocListener);
                textAjoute = "";
                colorRouge.add(new ArrayList<>(List.of(length, liste.get(0).replaceAll("\\s+$", "").length())));
            }
            try {
                commandes.setCaretPosition(commandes.getText().length());
            } catch (IllegalArgumentException e) {
            }
            commandes.requestFocusInWindow();
            colorText();
            if (requestFocus) {
                requestFocus = false;
                pathInput.requestFocusInWindow();
            }
        });
    }
    public static void implementText(String path) {
        SwingWorker<Void, String> worker = new SwingWorker<Void,String>() {
            @Override
            protected Void doInBackground() {
                return null;
            }
            @Override
            protected void process(List<String> chunks) {
            }
            @Override
            protected void done() {
                try {
                    String content = "";
                    FileReader filereader = new FileReader(path);
                    BufferedReader reader = new BufferedReader(filereader);
                    String line = reader.readLine();
                    while (line != null) {
                        content += line + "\n";
                        line = reader.readLine();
                    }
                    reader.close();
                    actualCode.setText(content);
                } catch (IOException err) {
                    err.printStackTrace();
                }
            }
        };
        worker.execute();
    }
    public static void changerStyle(JTextPane paneOr) {
        SwingWorker<Void, JTextPane> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                publish(paneOr);   
                return null;             
            }
            @Override
            protected void process(List<JTextPane> chunks) {
                for (JTextPane pane : chunks) {
                    if (updating) return;
                    updating = true;
                    try {
                        StyledDocument doc = pane.getStyledDocument();
                        Style Default = doc.addStyle("default", null);
                        StyleConstants.setBold(Default, true);
                        StyleConstants.setForeground(Default, new Color(0, 0, 0));
                        Style keyWordStyle = doc.addStyle("keyWordStyle", null);
                        StyleConstants.setBold(keyWordStyle, true);
                        StyleConstants.setForeground(keyWordStyle, new Color(128, 0, 128));
                        Style commentStyle = doc.addStyle("CommentStyle", null);
                        StyleConstants.setBold(commentStyle, true);
                        StyleConstants.setForeground(commentStyle, new Color(0, 100, 0));
                        Style entre = doc.addStyle("entre", null);
                        StyleConstants.setBold(entre, true);
                        StyleConstants.setForeground(entre, new Color(180, 110, 35));
                        Style input = doc.addStyle("input", null);
                        StyleConstants.setBold(input, true);
                        StyleConstants.setForeground(input, new Color(200, 125, 10));
                        Style nombres = doc.addStyle("nombres", null);
                        StyleConstants.setBold(nombres, true);
                        StyleConstants.setForeground(nombres, new Color(0, 125, 125));
                        Style balise = doc.addStyle("balise", null);
                        StyleConstants.setBold(balise, true);
                        StyleConstants.setForeground(balise, new Color(110, 110, 255));
                        String[]PreCorrectedText = pane.getText().split("\r\n");
                        String CorrectedText = "";
                        for (String e : PreCorrectedText) {
                            CorrectedText += e;
                        }
                        doc.setCharacterAttributes(0, pane.getText().length(), Default, true);
                        String regexbalises = "[<>()\\[\\]{}=!|]";
                        Pattern patternbalises = Pattern.compile(regexbalises);
                        Matcher matcherbalises = patternbalises.matcher(CorrectedText);
                        while (matcherbalises.find()) {
                            doc.setCharacterAttributes(matcherbalises.start(), matcherbalises.end() - matcherbalises.start(), balise, true);
                        }
                        String regexbalises3 = "[;,*/+-.:]";
                        Pattern patternbalises3 = Pattern.compile(regexbalises3);
                        Matcher matcherbalises3 = patternbalises3.matcher(CorrectedText);
                        while (matcherbalises3.find()) {
                            doc.setCharacterAttributes(matcherbalises3.start(), matcherbalises3.end() - matcherbalises3.start(), balise, true);
                        }
                        String regexbalises2 = "\\b(" + String.join("|", new String[]{"String", "string", "Boolean", "boolean", "bool", "Integer", "int", "Double", "float", "null", "void", "Void", "List", "HashMap", "none", "any", "str", "char", "p", "div", "table", "head", "body", "html", "element", "cd", "javac", "java", "python", "c++", "gcc", "ruby", "rust", "not", "and", "or", "let", "const", "node"}) + ")\\b";
                        Pattern patternbalises2 = Pattern.compile(regexbalises2);
                        Matcher matcherbalises2 = patternbalises2.matcher(CorrectedText);
                        while (matcherbalises2.find()) {
                            doc.setCharacterAttributes(matcherbalises2.start(), matcherbalises2.end() - matcherbalises2.start(), balise, true);
                        }
                        String[] keyWords = {"else", "if", "elif", "import", "for", "except", "catch", "def", "func", "try", "public", "private", "include", "return", "continue", "break", "execute", "run", "while", "each", "main"};
                        String regexnombre = "\\b([0-9]*)\\b";
                        Pattern patternnombre = Pattern.compile(regexnombre);
                        Matcher matchernombre = patternnombre.matcher(CorrectedText);
                        while (matchernombre.find()) {
                            doc.setCharacterAttributes(matchernombre.start(), matchernombre.end() - matchernombre.start(), nombres, true);
                        }
                        String regex = "\\b(" + String.join("|", keyWords) + ")\\b";
                        Pattern pattern = Pattern.compile(regex);
                        Matcher matcher = pattern.matcher(CorrectedText);
                        while (matcher.find()) {
                            doc.setCharacterAttributes(matcher.start(), matcher.end() - matcher.start(), keyWordStyle, true);
                        }
                        String[] regexentre = {"'[^'\r\n]*'", "`[^`\n\r]*`", "```[^```\n\r]*```","\"[^\"\n\r]*\""};
                        for (String element : regexentre) {
                            Pattern patternentre = Pattern.compile(element);
                            Matcher matcherentre = patternentre.matcher(CorrectedText);
                            while (matcherentre.find()) {
                                doc.setCharacterAttributes(matcherentre.start() , matcherentre.end() - matcherentre.start(), entre, true);
                            }
                        }
                        String[] regexcomment = {"#([^\n]*)", "//([^\n]*)", "/\\*([^*]*)\\*/", "<\\--([^*]*)\\-->", "\"\"\"([^\"]|\\n)*\"\"\""};
                        for (String element : regexcomment) {
                            Pattern patterncomment = Pattern.compile(element);
                            Matcher matchercomment = patterncomment.matcher(CorrectedText);
                            while (matchercomment.find()) {
                                doc.setCharacterAttributes(matchercomment.start(), matchercomment.end() - matchercomment.start(), commentStyle, true);
                            }
                        }        
                    } finally {
                        updating = false;
                    }
                }
            } 
            @Override
            protected void done() {
            }
        }; 
        worker.execute();    
    }
    public static void clickerSurPath(){
        for (JPanel p : map.keySet()) {
            if (p.getMouseListeners().length == 0) {
                p.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        JPanel panel = (JPanel) e.getSource();
                        panel.setBackground(Color.LIGHT_GRAY);
                        panel.repaint();
                    }
                    @Override
                    public void mouseExited(MouseEvent e) {
                        JPanel panel = (JPanel) e.getSource();
                        panel.setBackground(Color.WHITE);
                        panel.repaint();
                    }
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        JPanel panel = (JPanel) e.getSource();
                        String chemin = map.get(panel);
                        if (chemin != null) { 
                            try { 
                                File file = new File(chemin);
                                if (file.exists() && file.isDirectory()) {
                                    pathInput.setText(chemin);
                                    listerFichiers();
                                } else if (file.exists() && file.isFile()) {
                                    JPanel panneau = new JPanel();
                                    for (Component comp : fenetres.getComponents()) {
                                        fenetres.remove(comp);
                                    }
                                    ImageIcon Preimage = new ImageIcon("fichier2.png");
                                    Image temp = Preimage.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
                                    ImageIcon imageDef = new ImageIcon(temp);
                                    ImageIcon Preimage2 = new ImageIcon("fermer.jpg");
                                    Image temp2 = Preimage2.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
                                    ImageIcon imageDef2 = new ImageIcon(temp2);
                                    JLabel l = new JLabel(imageDef);
                                    JLabel l2 = new JLabel(file.getName());
                                    JButton bouton = new JButton(imageDef2);
                                    bouton.setBorderPainted(false);
                                    bouton.setFocusPainted(false);
                                    bouton.setContentAreaFilled(false);
                                    bouton.setBackground(Color.WHITE);
                                    bouton.setBounds(165, 2, 35, 35);
                                    bouton.setBorder(null);
                                    l2.setForeground(Color.DARK_GRAY);
                                    l2.setFont(new Font("Arial", Font.BOLD, 14));
                                    panneau.setLayout(null);
                                    l.setBounds(0, 0, 40, 40);
                                    l2.setBounds(42, 0, 120, 40);
                                    panneau.add(l2);
                                    panneau.add(l);
                                    panneau.add(bouton);
                                    panneau.setBackground(Color.WHITE);
                                    panneau.setBounds(0, 0, 200, 60);
                                    fenetres.add(panneau);
                                    int x = 200;
                                    for (JPanel j : widgets.keySet()) {
                                        j.setBounds(x, 0, 200, 60);
                                        fenetres.add(j);
                                        x += 200;
                                    }
                                    List<String> contenu = new ArrayList<>();
                                    contenu.add(chemin);
                                    contenu.add("selected");
                                    for (JPanel pa : widgets.keySet()) {
                                        widgets.get(pa).set(1, "passive");
                                    }
                                    widgets.put(panneau, contenu);
                                    for (JPanel key : widgets.keySet()) {
                                        if (widgets.get(key).get(1) == "selected") {
                                            numero = 500;
                                            String textNumero = "";
                                            for (int i = 1; i < numero; i++) {
                                                textNumero += String.valueOf(i) + "\n";
                                            }
                                            numerotation.setText(textNumero);
                                            implementText(widgets.get(key).get(0));
                                            break;
                                        }
                                    }
                                    fenetres.revalidate();
                                    fenetres.repaint();
                                    fenetres.setPreferredSize(new Dimension(x, 60));
                                    for (Component c2 : fenetres.getComponents()) {
                                        if (c2 instanceof JPanel) {
                                            List<Boolean> exit = new ArrayList<>();
                                            exit.add(false);
                                            JPanel label2 = (JPanel) c2;
                                            for (Component l3 : label2.getComponents()) {
                                                if (l3 instanceof JButton) {
                                                    JButton bouton2 = (JButton) l3;
                                                    if (bouton2.getActionListeners().length == 0) {
                                                        bouton2.addActionListener(e2 -> {
                                                            bouton2.setBackground(Color.WHITE);
                                                            widgets.remove(c2);
                                                            exit.set(0, true);
                                                            fenetres.removeAll();
                                                            int y = 0;
                                                            for (JPanel j : widgets.keySet()) {
                                                                j.setBounds(y, 0, 200, 60);
                                                                fenetres.add(j);
                                                                y += 200;
                                                            }
                                                            fenetres.revalidate();
                                                            fenetres.repaint();
                                                        });
                                                    }
                                                }
                                            }
                                            if (!exit.get(0)) {
                                                if (label2.getMouseListeners().length == 0) {
                                                    label2.addMouseListener( new MouseAdapter() {
                                                        @Override
                                                        public void mouseEntered(MouseEvent e) {
                                                            JPanel panel2 = (JPanel) e.getSource();
                                                            panel2.setBackground(Color.LIGHT_GRAY);
                                                            for (Component cc : panel2.getComponents()) {
                                                                if (cc instanceof JButton) {
                                                                    cc.setBackground(Color.LIGHT_GRAY);
                                                                }
                                                            }
                                                            panel2.repaint();
                                                        }
                                                        @Override
                                                        public void mouseExited(MouseEvent e) {
                                                            JPanel panel2 = (JPanel) e.getSource();
                                                            panel2.setBackground(Color.WHITE);
                                                            for (Component cc : panel2.getComponents()) {
                                                                if (cc instanceof JButton) {
                                                                    cc.setBackground(Color.WHITE);
                                                                }
                                                            }
                                                            panel2.repaint();
                                                        }
                                                        @Override
                                                        public void mouseClicked(MouseEvent e) {
                                                            String path = "";
                                                            String path2 = "";
                                                            for (JPanel key : widgets.keySet()) {
                                                                if (widgets.get(key).get(1).equals("selected")) {
                                                                    path = widgets.get(key).get(0);
                                                                    break;
                                                                }
                                                            }
                                                            for (JPanel key : widgets.keySet()) {
                                                                if (key != label2) {
                                                                    widgets.get(key).set(1, "passive");
                                                                } else {
                                                                    widgets.get(key).set(1, "selected");
                                                                    path2 = widgets.get(key).get(0);
                                                                }
                                                            }
                                                            if (!(path.equals(path2))) {
                                                                for (JPanel key : widgets.keySet()) {
                                                                    if (widgets.get(key).get(1) == "selected") {
                                                                        numero = 500;
                                                                        String textNumero = "";
                                                                        for (int i = 1; i < numero; i++) {
                                                                            textNumero += String.valueOf(i) + "\n";
                                                                        }
                                                                        numerotation.setText(textNumero);
                                                                        implementText(widgets.get(key).get(0));
                                                                        break;
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    });
                                                }
                                            }
                                        }
                                    }
                                }
                            } catch (NullPointerException | SecurityException err) {
                                err.printStackTrace();
                            }
                        }
                    }
                });
            }
        }
    }
    public static JScrollPane changeUI(JScrollPane pane) {
        pane.getVerticalScrollBar().setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(225, 225, 225); 
                this.trackColor = Color.WHITE; 
            }

            @Override
            protected JButton createDecreaseButton(int orientation) {
                return new JButton() { 
                    @Override
                    public Dimension getPreferredSize() {
                        return new Dimension(0, 0);
                    }
                };
            }

            @Override
            protected JButton createIncreaseButton(int orientation) {
                return new JButton() { 
                    @Override
                    public Dimension getPreferredSize() {
                        return new Dimension(0, 0);
                    }
                };
            }
        });
        pane.getHorizontalScrollBar().setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(225, 225, 225); 
                this.trackColor = Color.WHITE; 
            }
            @Override
            protected JButton createDecreaseButton(int orientation) {
                return new JButton() { 
                    @Override
                    public Dimension getPreferredSize() {
                        return new Dimension(0, 0);
                    }
                };
            }

            @Override
            protected JButton createIncreaseButton(int orientation) {
                return new JButton() { 
                    @Override
                    public Dimension getPreferredSize() {
                        return new Dimension(0, 0);
                    }
                };
            }
        });
        return pane;
    }
    public static void listerFichiers() {
        String contenu = pathInput.getText();
        SwingWorker<Void, String> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                File directory = new File(contenu);
                if (directory.exists() && directory.isDirectory()) {
                    if (repeatRequest) {
                        SwingUtilities.invokeLater(() -> {
                            String NewtextAjoute = " cd \"" + contenu + "\"";
                            if (!(addedText.equals(NewtextAjoute))) {
                                addedText = NewtextAjoute;
                                Document document = commandes.getDocument();
                                document.removeDocumentListener(DocListener);
                                try {
                                    int length = commandes.getText().length();
                                    TextSauvegarde = commandes.getText();
                                    document.insertString(commandes.getText().length(), NewtextAjoute, input);
                                    document.insertString(commandes.getText().length(), "\n", defStyle);
                                    textAjoute = "";
                                    colorJaune.add(new ArrayList<>(List.of(length, NewtextAjoute.length())));
                                    communicateWithCMD(NewtextAjoute);
                                    requestFocus = true;
                                    colorText();
                                } catch (BadLocationException err) {
                                    err.printStackTrace();
                                }
                                document.addDocumentListener(DocListener); 
                            }
                        });
                    } else {
                        repeatRequest = true;
                    }
                    map.clear();
                    File[] fichiers = directory.listFiles();
                    if (fichiers.length > 0) {
                        for (File fichier : fichiers) {
                            if (fichier.getPath() != null) {
                                publish(fichier.getPath());
                            }
                        }
                    } else {
                        panelPaths.removeAll();
                        panelPaths.revalidate();
                        panelPaths.repaint();
                    }
                } 
                return null;
            }
            @Override
            protected void process(List<String> results) {
                panelPaths.removeAll();
                panelPaths.revalidate();
                panelPaths.repaint();
                int bounds = 0;
                int size = 0;
                int intermediaire;
                for (String result : results) {
                    JPanel panel = new JPanel();
                    File file = new File(result);
                    panel.setBounds(5, bounds, 40 + (file.getName().length() * 15), 45);
                    panel.setBackground(Color.WHITE);
                    panel.setLayout(null);
                    intermediaire = file.getName().length() * 8;
                    if (intermediaire > size) {
                        size = intermediaire;
                    }
                    if (file.exists() && file.isDirectory()) {
                        ImageIcon Preimage = new ImageIcon("fichier.png");
                        Image temp = Preimage.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
                        ImageIcon imageDef = new ImageIcon(temp);
                        JLabel label = new JLabel(imageDef);
                        label.setBounds(2, 2, 40, 40);
                        JLabel label2 = new JLabel(file.getName());
                        label2.setBounds(44, 0, (file.getName().length() * 15), 40);
                        label2.setFont(new Font("Arial", Font.PLAIN, 15));
                        label2.setForeground(Color.BLACK);
                        panel.add(label);
                        panel.add(label2);
                        panelPaths.add(panel);
                        panelPaths.revalidate();
                        panelPaths.repaint();
                    } else if (file.exists() && file.isFile()) {
                        ImageIcon Preimage = new ImageIcon("fichier2.png");
                        Image temp = Preimage.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
                        ImageIcon imageDef = new ImageIcon(temp);
                        JLabel label = new JLabel(imageDef);
                        label.setBounds(2, 2, 40, 40);
                        JLabel label2 = new JLabel(file.getName());
                        label2.setBounds(44, 0, (file.getName().length() * 15), 40);
                        label2.setFont(new Font("Arial", Font.PLAIN, 15));
                        label2.setForeground(Color.BLACK);
                        panel.add(label);
                        panel.add(label2);
                        panelPaths.add(panel);
                        panelPaths.revalidate();
                        panelPaths.repaint();
                    }  
                    map.put(panel, file.getPath());
                    bounds += 50;  
                }
                panelPaths.setPreferredSize(new Dimension(40 + size, bounds));
                bounds = 0;
            }
            @Override
            protected void done() {
                clickerSurPath();
            }
        };
        worker.execute();
    }
    private static String textFinal = "";
    public static void appendText(String text, String ajout, int offset, JTextPane actualCode) {
        textFinal = "";
        int i2 = 0;
        for (int i = 0; i < text.length(); i++) {
            textFinal += String.valueOf(text.charAt(i));
            i2 = i;
            if (i == offset) {
                break;
            }
        }
        textFinal += ajout;
        for (int o = i2 + 1 ; o < text.length(); o++) {
            textFinal += String.valueOf(text.charAt(o));
        }
        actualCode.setText(textFinal);
        actualCode.setCaretPosition(i2 + 1);
        actualCode.requestFocusInWindow();
    }
    private static JScrollPane scrollPane;
    private static JScrollPane scrollPane2;
    private static JScrollPane scrollPane3;
    private static JScrollPane scrollPane4;
    private static int numero = 500;
    private static JTextPane numerotation;
    private static JTextPane commandes;
    private static String TextSauvegarde = "";
    private static String textAjoute = "";
    public static Style input;
    public static Style error;
    public static Style defStyle;
    public static StyledDocument doc;
    public static JTextField pathInput;
    private static DocumentListener DocListener = new DocumentListener() {
        @Override
        public void insertUpdate(DocumentEvent e) {
            FontMetrics fm = commandes.getFontMetrics(commandes.getFont());
            String plusgrandeligne = "";
            String[] lignes = commandes.getText().split("\n");
            for (int i = 0; i < lignes.length; i++) {
                for (int o  = 0; o < lignes.length; o++) {
                    if (o > 0) {
                        if (lignes[o].length() > lignes[o - 1].length()) {
                            String intermediaire = lignes[o];
                            lignes[o] = lignes[o - 1];                        
                            lignes[o - 1] = intermediaire;
                        } 
                    }
                }
            }
            if (lignes.length > 0) {
                plusgrandeligne = lignes[0];
            }
            plusgrandeligne += "      ";
            SwingUtilities.invokeLater(() -> {
                try {
                    String ajout = commandes.getText(e.getOffset(), e.getLength());
                    if (ajout.equals("\n")) {
                        if ((TextSauvegarde + textAjoute + "\n").equals(commandes.getText())) {
                            communicateWithCMD(textAjoute);
                            TextSauvegarde = commandes.getText();
                            textAjoute = "";
                        } else {
                            commandes.getDocument().removeDocumentListener(DocListener);
                            commandes.setText(TextSauvegarde + textAjoute);
                            commandes.getDocument().addDocumentListener(DocListener);
                            TextSauvegarde = commandes.getText();
                            textAjoute = "";
                        }
                    } else {
                        String SauvegardeSupposee = "";
                        SauvegardeSupposee = commandes.getText(0, TextSauvegarde.length());
                        if (SauvegardeSupposee.equals(TextSauvegarde)) {
                            textAjoute = commandes.getText(TextSauvegarde.length(), commandes.getText().length() - TextSauvegarde.length());
                            if (colorJaune.size() > 0) {                        
                                if (!(colorJaune.get(colorJaune.size() - 1).get(0) == TextSauvegarde.length())) {
                                    colorJaune.add(new ArrayList<>(List.of(TextSauvegarde.length(), textAjoute.length())));
                                } else {
                                    colorJaune.get(colorJaune.size() - 1).set(1, textAjoute.length());
                                }
                            } else {
                                colorJaune.add(new ArrayList<>(List.of(TextSauvegarde.length(), textAjoute.length())));
                            }
                        } else {
                            commandes.getDocument().removeDocumentListener(DocListener);
                            commandes.setText(TextSauvegarde + textAjoute);
                            commandes.getDocument().addDocumentListener(DocListener);
                        }
                    }
                } catch (BadLocationException err) {
                    err.printStackTrace();               
                } finally {
                    colorText();
                }
            });
            int width = fm.stringWidth(plusgrandeligne);
            int newWidth = Math.max(200, width);
            int lineHeight = fm.getHeight();
            int lineCount = (commandes.getText() + "\n ").split("\n").length;
            commandes.setPreferredSize(new Dimension(newWidth, (Math.max(100, lineHeight*lineCount + 20)) + 50));
            commandes.revalidate();
            commandes.repaint();
            scrollPane4.revalidate();
            scrollPane4.repaint();
            colorText();
        }
        @Override
        public void removeUpdate(DocumentEvent e) {
            FontMetrics fm = commandes.getFontMetrics(commandes.getFont());
            String plusgrandeligne = "";
            String[] lignes = commandes.getText().split("\n");
            for (int i = 0; i < lignes.length; i++) {
                for (int o  = 0; o < lignes.length; o++) {
                    if (o > 0) {
                        if (lignes[o].length() > lignes[o - 1].length()) {
                            String intermediaire = lignes[o];
                            lignes[o] = lignes[o - 1];                        
                            lignes[o - 1] = intermediaire;
                        } 
                    }
                }
            }
            plusgrandeligne = lignes[0];
            plusgrandeligne += "      ";
            SwingUtilities.invokeLater(() -> {
                try {
                    String TextSauvegardeTemp1 = TextSauvegarde.replaceAll("[\n\r ]+$", "");
                    int length = Math.min(commandes.getText().length(), TextSauvegarde.length());
                    String TextSauvegardeTemp2 = commandes.getText(0, length).replaceAll("[\n\r ]+$", "");
                    if (TextSauvegardeTemp1.equals(TextSauvegardeTemp2)) {
                        if (!textAjoute.equals("")) {
                            if (!(commandes.getText().length() - TextSauvegarde.length() < 0)) {
                                String newTextAjoute = "";
                                newTextAjoute = commandes.getText(TextSauvegarde.length(), commandes.getText().length()-TextSauvegarde.length());
                                if (colorJaune.size() > 0) {                        
                                    if (!(colorJaune.get(colorJaune.size() - 1).get(0) == TextSauvegarde.length())) {
                                        colorJaune.add(new ArrayList<>(List.of(TextSauvegarde.length(), newTextAjoute.length())));
                                    } else {
                                        colorJaune.get(colorJaune.size() - 1).set(1, newTextAjoute.length());
                                    }
                                } else {
                                    colorJaune.add(new ArrayList<>(List.of(TextSauvegardeTemp1.length(), newTextAjoute.length())));
                                }
                                textAjoute = newTextAjoute;
                            } else {
                                textAjoute = "";
                                int index = -1;
                                for (int i = 0; i < colorJaune.size(); i++) {
                                    if (colorJaune.get(i).get(0) == TextSauvegarde.length()) {
                                        index = i;
                                    }
                                }
                                if (index != -1) {
                                    colorJaune.remove(index);
                                }
                                TextSauvegarde = TextSauvegardeTemp1;
                            }
                        } else {
                            TextSauvegarde = commandes.getText();
                        }
                    } else {
                        commandes.getDocument().removeDocumentListener(DocListener);
                        commandes.setText(TextSauvegarde + textAjoute);
                        commandes.getDocument().addDocumentListener(DocListener);
                    }
                } catch(BadLocationException err) {
                    err.printStackTrace();
                    commandes.getDocument().removeDocumentListener(DocListener);
                    commandes.setText(TextSauvegarde + textAjoute);
                    commandes.getDocument().addDocumentListener(DocListener);
                } finally {
                    colorText();
                }
            });
            int width = fm.stringWidth(plusgrandeligne);
            int newWidth = Math.max(200, width);
            int lineHeight = fm.getHeight();
            int lineCount = (commandes.getText() + "\n ").split("\n").length;
            commandes.setPreferredSize(new Dimension(newWidth, (Math.max(100, lineHeight*lineCount + 20)) + 50));
            commandes.revalidate();
            commandes.repaint();
            scrollPane4.revalidate();
            scrollPane4.repaint();
            colorText();
        }
        @Override
        public void changedUpdate(DocumentEvent e) {
        }
    };
    public static void main(String[] args) {
        frame = new JFrame("Light and Smart Code Editor (LSCE)");
        ImageIcon icon = new ImageIcon("codeEditor.png");
        frame.setIconImage(icon.getImage());
        frame.setSize(1350, 800);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setBackground(Color.WHITE);
        frame.setLayout(null);
        frame.getContentPane().setBackground(Color.WHITE);
        JLabel labelChargement = new JLabel("Chargement...");
        labelChargement.setForeground(Color.DARK_GRAY);
        labelChargement.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 40));
        labelChargement.setBounds(frame.getWidth() / 2 - 150, frame.getHeight() / 2 - 60, 300, 60);
        frame.add(labelChargement);
        frame.setVisible(true);
        JPanel panel = new JPanel();
        panel.setBounds(5, 5, (frame.getWidth() * 15/100), (frame.getHeight() * 7/100));
        panel.setBackground(Color.WHITE);
        panel.setLayout(new FlowLayout(FlowLayout.LEFT, 6, 1));
        ImageIcon image1 = new ImageIcon("ajouterFichier.jpg");
        ImageIcon image1_2 = new ImageIcon(image1.getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH));
        JButton bouton1 = new JButton(image1_2);
        bouton1.setText("");
        bouton1.setBackground(Color.WHITE);
        bouton1.setBorder(null);
        panel.add(bouton1); 
        ImageIcon image2 = new ImageIcon("ajouterDossier.jpg");
        ImageIcon image2_2 = new ImageIcon(image2.getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH));
        JButton bouton2 = new JButton(image2_2);
        bouton2.setText("");
        bouton2.setBackground(Color.WHITE);
        bouton2.setBorder(null);
        panel.add(bouton2);
        ImageIcon image3 = new ImageIcon("enrengister.jpg");
        ImageIcon image3_2 = new ImageIcon(image3.getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH));
        JButton bouton3 = new JButton(image3_2);
        bouton3.setText("");
        bouton3.setBackground(Color.WHITE);
        bouton3.setBorder(null);
        panel.add(bouton3);
        ImageIcon image4 = new ImageIcon("renommer.jpg");
        ImageIcon image4_2 = new ImageIcon(image4.getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH));
        JButton bouton4 = new JButton(image4_2);
        bouton4.setText("");
        bouton4.setBackground(Color.WHITE);
        bouton4.setBorder(null);
        panel.add(bouton4);
        ImageIcon image5 = new ImageIcon("supprimer.png");
        ImageIcon image5_2 = new ImageIcon(image5.getImage().getScaledInstance(24, 26, Image.SCALE_SMOOTH));
        JButton bouton5 = new JButton(image5_2);
        bouton5.setText("");
        bouton5.setBackground(Color.WHITE);
        bouton5.setBorder(null);
        panel.add(bouton5);
        ImageIcon image6 = new ImageIcon("terminal.jpg");
        ImageIcon image6_2 = new ImageIcon(image6.getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH));
        JButton bouton6 = new JButton(image6_2);
        bouton6.setText("");
        bouton6.setBackground(Color.WHITE);
        bouton6.setBorder(null);
        panel.add(bouton6);
        ImageIcon image7 = new ImageIcon("run.png");
        ImageIcon image7_2 = new ImageIcon(image7.getImage().getScaledInstance(27, 19, Image.SCALE_SMOOTH));
        JButton bouton7 = new JButton(image7_2);
        bouton7.setText("");
        bouton7.setBackground(Color.WHITE);
        bouton7.setBorder(null);
        panel.add(bouton7);
        ImageIcon image8 = new ImageIcon("deleteAll.jpg");
        ImageIcon image8_2 = new ImageIcon(image8.getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH));
        JButton bouton8 = new JButton(image8_2);
        bouton8.setText("");
        bouton8.setBackground(Color.WHITE);
        bouton8.setBorder(null);
        panel.add(bouton8);
        ImageIcon image9 = new ImageIcon("renommerFichier.jpg");
        ImageIcon image9_2 = new ImageIcon(image9.getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH));
        JButton bouton9 = new JButton(image9_2);
        bouton9.setText("");
        bouton9.setBackground(Color.WHITE);
        bouton9.setBorder(null);
        panel.add(bouton9);
        ImageIcon image10 = new ImageIcon("supprimerDossier.jpg");
        ImageIcon image10_2 = new ImageIcon(image10.getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH));
        JButton bouton10 = new JButton(image10_2);
        bouton10.setText("");
        bouton10.setBackground(Color.WHITE);
        bouton10.setBorder(null);
        panel.add(bouton10);
        frame.add(panel);
        fenetres = new JPanel();
        scrollPane2 = new JScrollPane(fenetres);
        scrollPane2.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollPane2.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scrollPane2.getHorizontalScrollBar().setPreferredSize(new Dimension(0, 5));
        scrollPane2.setBounds((frame.getWidth() * 15/100) + 15, 5, (frame.getWidth() * 82/100), 60);
        scrollPane2.setBackground(Color.WHITE);
        scrollPane2.setBorder(null);
        scrollPane2 = changeUI(scrollPane2);
        fenetres.setBackground(Color.WHITE);
        fenetres.setLayout(null);
        frame.add(scrollPane2);
        panelPaths = new JPanel();
        scrollPane = new JScrollPane(panelPaths);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        panelPaths.setBackground(Color.WHITE);
        scrollPane.setBounds(5, (frame.getHeight() * 10/100) + 15, (frame.getWidth() * 15/100), (frame.getHeight() * 82/100));
        panelPaths.setLayout(null);
        scrollPane.setBorder(null);
        scrollPane.setBackground(Color.WHITE);
        scrollPane = changeUI(scrollPane);
        frame.add(scrollPane);      
        pathInput = new JTextField();
        pathInput.setPreferredSize(new Dimension(200, 30));
        pathInput.setFont(new Font("Arial", Font.BOLD, 16));
        pathInput.setForeground(Color.BLACK);
        pathInput.setBounds(5, (frame.getHeight() * 7/100) + 10, (frame.getWidth() * 15/100), (frame.getHeight() * 3/100));
        frame.add(pathInput);
        pathInput.getDocument().addDocumentListener(new DocumentListener() {
            @Override 
            public void insertUpdate(DocumentEvent e) {
                listerFichiers();
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                listerFichiers();
            }
            @Override 
            public void changedUpdate(DocumentEvent e) {
            }
        });
        JPanel code = new JPanel();
        scrollPane3 = new JScrollPane(code);
        scrollPane3.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane3.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane3.setBorder(null);
        scrollPane3.setBackground(Color.WHITE);
        scrollPane3.setBounds((frame.getWidth() * 15/100) + 15, (frame.getHeight() * 5/100) + 30, (frame.getWidth() * 82/100), (frame.getHeight() * 60/100));
        code.setBackground(Color.WHITE);
        code.setLayout(new BorderLayout());
        scrollPane3 = changeUI(scrollPane3);
        numerotation = new JTextPane();
        numerotation.setBackground(Color.WHITE);
        numerotation.setForeground(Color.DARK_GRAY);
        numerotation.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 25));
        numerotation.setEditable(false);
        numerotation.setPreferredSize(new Dimension(75, (frame.getHeight() * 60/100)));
        String temp = "";
        for (int i = 1; i < numero; i++) {
            temp += i + "\n";
        }
        numerotation.setText(temp);
        code.add(numerotation, BorderLayout.WEST);
        actualCode.setBackground(Color.WHITE);
        actualCode.setForeground(Color.BLACK);
        actualCode.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 25));
        actualCode.setPreferredSize(new Dimension((frame.getWidth() * 70/100), (frame.getHeight() * 60/100)));        
        actualCode.setAutoscrolls(true); 
        actualCode.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                SwingUtilities.invokeLater(() -> {
                    try {
                        String textAjoute = actualCode.getText(e.getOffset(), e.getLength());
                        if (textAjoute.equals("(")) {
                            appendText(actualCode.getText(), ")", e.getOffset(), actualCode);
                        } else if (textAjoute.equals("[")) {
                            appendText(actualCode.getText(), "]", e.getOffset(), actualCode);
                        } else if (textAjoute.equals("{")) {
                            appendText(actualCode.getText(), "}", e.getOffset(), actualCode);
                        } else if (textAjoute.equals("\"")) {
                            appendText(actualCode.getText(), "\"", e.getOffset(), actualCode);
                        } else if (textAjoute.equals("'")) {
                            appendText(actualCode.getText(), "'", e.getOffset(), actualCode);
                        } else if (textAjoute.equals("`")) {
                            appendText(actualCode.getText(), "`", e.getOffset(), actualCode);
                        }
                    } catch (BadLocationException | IllegalStateException err) {
                        err.printStackTrace();
                    }
                });
                FontMetrics fm = actualCode.getFontMetrics(actualCode.getFont());
                String plusgrandeligne = "";
                String[] lignes = actualCode.getText().split("\n");
                for (int i = 0; i < lignes.length; i++) {
                    for (int o  = 0; o < lignes.length; o++) {
                        if (o > 0) {
                            if (lignes[o].length() > lignes[o - 1].length()) {
                                String intermediaire = lignes[o];
                                lignes[o] = lignes[o - 1];                        
                                lignes[o - 1] = intermediaire;
                            } 
                        }
                    }
                }
                if (lignes.length > 0) {
                    plusgrandeligne = lignes[0];
                } 
                plusgrandeligne += "    ";
                int textWidth = fm.stringWidth(plusgrandeligne); 
                int newWidth = Math.max(200, textWidth + 20); 
                if (lignes.length > numero - 50) {
                    int Pre_numero = numero;
                    while (lignes.length > numero - 50) {
                        numero += 2500;
                    }
                    String textNumero = numerotation.getText();
                    for (int i = Pre_numero; i < numero; i++) {
                        textNumero += String.valueOf(i) + "\n";
                    }
                    numerotation.setText(textNumero);
                }
                int lineHeight = fm.getHeight();
                int lineCount = (actualCode.getText() + "\n \n \n \n \n").split("\n").length;
                int newHeight = Math.max(100, lineCount * lineHeight);
                actualCode.setPreferredSize(new Dimension(newWidth + 50, newHeight + 50));
                SwingUtilities.invokeLater(() -> {
                    changerStyle(actualCode);
                });
            } 
            @Override
            public void removeUpdate(DocumentEvent e) {
                FontMetrics fm = actualCode.getFontMetrics(actualCode.getFont());
                String plusgrandeligne = "";
                String[] lignes = actualCode.getText().split("\n");
                for (int i = 0; i < lignes.length; i++) {
                    for (int o  = 0; o < lignes.length; o++) {
                        if (o > 0) {
                            if (lignes[o].length() > lignes[o - 1].length()) {
                                String intermediaire = lignes[o];
                                lignes[o] = lignes[o - 1];                        
                                lignes[o - 1] = intermediaire;
                            } 
                        }
                    }
                }
                if (lignes.length > 0) {
                    plusgrandeligne = lignes[0];
                }
                plusgrandeligne += "     ";
                int textWidth = fm.stringWidth(plusgrandeligne); 
                int newWidth = Math.max(200, textWidth + 20);    
                if (lignes.length > numero - 50) {
                    int Pre_numero = numero;
                    while (lignes.length > numero - 50) {
                        numero += 500;
                    }
                    String textNumero = numerotation.getText();
                    for (int i = Pre_numero; i < numero; i++) {
                        textNumero += String.valueOf(i) + "\n";
                    }
                    numerotation.setText(textNumero);
                }
                int lineHeight = fm.getHeight();
                int lineCount = (actualCode.getText() + "\n \n \n \n \n").split("\n").length;
                int newHeight = Math.max(100, lineCount * lineHeight + 20);
                actualCode.setPreferredSize(new Dimension(newWidth + 50, newHeight + 50));
                SwingUtilities.invokeLater(() -> {
                    changerStyle(actualCode);
                });
            } 
            @Override
            public void changedUpdate(DocumentEvent e) {
            } 
        });
        code.add(actualCode, BorderLayout.CENTER);
        frame.add(scrollPane3);
        commandes = new JTextPane();
        scrollPane4 = new JScrollPane(commandes);
        scrollPane4.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane4.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane4.setBounds((frame.getWidth() * 15/100) + 15, (frame.getHeight() * 70/100) + 35, (frame.getWidth() * 82/100), (frame.getHeight() * 20/100));
        scrollPane4.setBackground(Color.WHITE);
        scrollPane4.setBorder(null);
        scrollPane4 = changeUI(scrollPane4);
        commandes.setBackground(Color.WHITE);
        commandes.setForeground(new Color(0, 0, 0));
        commandes.setFont(new Font("Consolas", Font.PLAIN, 18));
        commandes.setEditable(true);
        commandes.setPreferredSize(new Dimension((frame.getWidth() * 82/100), (frame.getHeight() * 15/100)));
        frame.add(scrollPane4);
        commandes.getDocument().addDocumentListener(DocListener);
        frame.remove(labelChargement);
        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                pathInput.setBounds(5, (frame.getHeight() * 7/100) + 10, (frame.getWidth() * 15/100), (frame.getHeight() * 3/100));
                panel.setBounds(5, 5, (frame.getWidth() * 15/100), (frame.getHeight() * 7/100));
                scrollPane3.setBounds((frame.getWidth() * 15/100) + 15, (frame.getHeight() * 5/100) + 35, (frame.getWidth() * 82/100), (frame.getHeight() * 65/100));
                scrollPane2.setBounds((frame.getWidth() * 15/100) + 15, 5, (frame.getWidth() * 82/100), 60);
                scrollPane4.setBounds((frame.getWidth() * 15/100) + 15, (frame.getHeight() * 70/100) + 35, (frame.getWidth() * 82/100), (frame.getHeight() * 20/100));
                scrollPane.setBounds(5, (frame.getHeight() * 10/100) + 15, (frame.getWidth() * 15/100), (frame.getHeight() * 82/100));
                frame.revalidate();
                frame.repaint();
            }
        });  
        frame.revalidate();
        frame.repaint();
        doc = commandes.getStyledDocument();
        input = doc.addStyle("input", null);
        StyleConstants.setForeground(input, new Color(180, 180, 0));
        StyleConstants.setBold(input, true);
        error = doc.addStyle("error", null);
        StyleConstants.setBold(error, true);
        StyleConstants.setForeground(error, new Color(150, 50, 50));
        defStyle = doc.addStyle("error", null);
        StyleConstants.setBold(defStyle, false);
        StyleConstants.setForeground(defStyle, new Color(0, 0, 0));
        terminal = new codeEditor();
        terminal.CreateTerminal();
        Runnable task = () -> {
            try {
                while (true) {
                    Thread.sleep(50);
                    String output = terminal.ReadOutput();
                    String outputError = terminal.ReadError();
                    if (!(outputError.equals(""))) {
                        if (!(outputError.equals(">>> "))) {
                            updateCMD(new ArrayList<>(List.of(outputError, "error")));
                        } else {
                            output = output.replaceAll("^[\\s+]*(" + Pattern.quote(command) + "\\n)", "");
                            updateCMD(new ArrayList<>(List.of(output, "output")));
                            updateCMD(new ArrayList<>(List.of(outputError, "error")));
                            output = "";
                        }
                    } 
                    if (!(output.equals(""))) {
                        output = output.replaceAll("^[\\s+]*(" + Pattern.quote(command) + "\\n)", "");
                        updateCMD(new ArrayList<>(List.of(output, "output")));
                    } 
                }
            } catch (InterruptedException e) {
            }
        };
        read = new Thread(task);
        read.start();
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                terminal.CloseTerminal();
                read.interrupt();
            }
        });
        bouton1.addActionListener(e -> {
            createFile = true;
            Document document = commandes.getDocument();
            try {
                String AjoutTextAjoute = " type nul > nouveauFichier.txt & :: vous pouvez modifier le nom/type du fichier.";
                document.insertString(commandes.getText().length(), AjoutTextAjoute, input);
            } catch (BadLocationException err) {
                err.printStackTrace();
            }
            repeatRequest = false;
        });
        bouton2.addActionListener(e -> {
            createFile = true;
            Document document = commandes.getDocument();
            try {
                String AjoutTextAjoute = " mkdir nouveauDossier & :: vous pouvez modifier le nom du dossier.";
                document.insertString(commandes.getText().length(), AjoutTextAjoute, input);
            } catch (BadLocationException err) {
                err.printStackTrace();
            }
            repeatRequest = false;
        });
        bouton3.addActionListener(e -> {
            for (JPanel key : widgets.keySet()) {
                if (widgets.get(key).get(1).equals("selected")) {
                    try {
                        FileWriter fileWriter = new FileWriter(widgets.get(key).get(0), false);
                        BufferedWriter writer = new BufferedWriter(fileWriter);
                        writer.write(actualCode.getText());
                        writer.close();
                    } catch (IOException err) {
                        err.printStackTrace();
                    }
                    break;
                }
            }
        });
        bouton4.addActionListener(e -> {  
            String nomDossier = "nomActuelDossier";
            File Dirfile = new File(pathInput.getText());
            if (Dirfile.exists() && Dirfile.isDirectory()) {
                for (File file : Dirfile.listFiles()) {
                    if (file.isDirectory()) {
                        nomDossier = file.getName();
                        break;
                    }
                }
            }
            Document document = commandes.getDocument();
            createFile = true;
            try {
                String AjoutTextAjoute = " ren " + "\""+ nomDossier +"\"" + " \"nouveauNomDossier\"" + " & :: entrez l'ancien nom (ou le chemin) du dossier et son nouveau nom.";
                document.insertString(TextSauvegarde.length(), AjoutTextAjoute, input);
            } catch (BadLocationException err) {
                err.printStackTrace();
            }
            repeatRequest = false;
        });
        bouton5.addActionListener(e -> {
            String nomFichier = "FichierASupprimer";
            for (JPanel key : widgets.keySet()) {
                if (widgets.get(key).get(1).equals("selected")) {
                    nomFichier = widgets.get(key).get(0);
                    break;
                }
            }
            Document document = commandes.getDocument();
            createFile = true;
            try {
                String AjoutTextAjoute = " del " + nomFichier + " & :: vous pouvez modifier le nom (ou le chemin) du fichier a supprimer.";
                document.insertString(commandes.getText().length(), AjoutTextAjoute, input);
            } catch (BadLocationException err) {
                err.printStackTrace();
            }
            document.addDocumentListener(DocListener);
            repeatRequest = false;
        });
        bouton6.addActionListener(e -> {
            terminal.CloseTerminal();
            read.interrupt();
            colorJaune.clear();
            colorRouge.clear();
            read = new Thread(task);
            TextSauvegarde = "";
            textAjoute = "";
            SwingUtilities.invokeLater(() -> commandes.setText(""));
            terminal.CreateTerminal();
            read.start();
        });
        bouton7.addActionListener(e -> {
            String textAExecuter = "";
            for (JPanel widget : widgets.keySet()) {
                if (widgets.get(widget).get(1).equals("selected")) {
                    String[] pathDecompose = widgets.get(widget).get(0).split("[/\\\\]");
                    String[] fichierDecompose = pathDecompose[pathDecompose.length - 1].split("\\.");
                    String type = fichierDecompose[fichierDecompose.length - 1];
                    if (type.equals("py")) {
                        textAExecuter = "python " + "\"" +  widgets.get(widget).get(0) + "\"";
                    } else if (type.equals("java")) {
                        String path = "";
                        for (int i = 0; i < pathDecompose.length - 1; i++) {
                            if (i == pathDecompose.length - 2) {
                                path += pathDecompose[i];
                            } else {
                                path += pathDecompose[i] + "\\";
                            }
                        }
                        String mot = "";
                        for (int i = 0; i < fichierDecompose.length - 1; i++) {
                            if (i == fichierDecompose.length - 2) {
                                mot += fichierDecompose[i];
                            } else {
                                mot += fichierDecompose[i] + ".";
                            }
                        }
                        textAExecuter = "javac " + "\"" +  widgets.get(widget).get(0) + "\"" + " && " + "java -cp " + "\"" + path + "\"" + " " + mot;
                    } else if (type.equals("js")) {
                        textAExecuter = "node " + "\"" +  widgets.get(widget).get(0) + "\"";
                    } else if (type.equals("c")) {
                        String path = "";
                        for (int i = 0; i < pathDecompose.length - 1; i++) {
                            if (i == pathDecompose.length - 2) {
                                path += pathDecompose[i];
                            } else {
                                path += pathDecompose[i] + "\\";
                            }
                        }
                        String mot = "";
                        for (int i = 0; i < fichierDecompose.length - 1; i++) {
                            if (i == fichierDecompose.length - 2) {
                                mot += fichierDecompose[i];
                            } else {
                                mot += fichierDecompose[i] + ".";
                            }
                        }
                        textAExecuter = "gcc " + "\"" +  widgets.get(widget).get(0) + "\"" +  " -o " + "\"" + path + mot + "\"" +  " && "  + "\"" + path + mot + "\"";
                    } else if (type.equals("cpp")) {
                        String path = "";
                        for (int i = 0; i < pathDecompose.length - 1; i++) {
                            path += pathDecompose[i] + "\\";
                        }
                        String mot = "";
                        for (int i = 0; i < fichierDecompose.length - 1; i++) {
                            if (i == fichierDecompose.length - 2) {
                                mot += fichierDecompose[i];
                            } else {
                                mot += fichierDecompose[i] + ".";
                            }
                        }
                        textAExecuter = "g++ " + "\"" +  widgets.get(widget).get(0) + "\"" +  " -o " + "\"" + path + mot + "\"" +  " && "  + "\"" + path + mot + "\"";
                    } else if (type.equals("cs")) {
                        String path = "";
                        for (int i = 0; i < pathDecompose.length - 1; i++) {
                            path += pathDecompose[i] + "\\";
                        }
                        String mot = "";
                        for (int i = 0; i < fichierDecompose.length - 1; i++) {
                            if (i == fichierDecompose.length - 2) {
                                mot += fichierDecompose[i];
                            } else {
                                mot += fichierDecompose[i] + ".";
                            }
                        }
                        textAExecuter = "csc -out:" + "\"" + path + mot + ".exe" + "\""  + " \"" +  widgets.get(widget).get(0) + "\"" +  " && "  + "\"" + path + mot + ".exe" + "\"";
                    } else if (type.equals("php")) {
                        textAExecuter = "php " + "\"" +  widgets.get(widget).get(0) + "\"";
                    } else if (type.equals("ts")) {
                        textAExecuter = "ts-node " + "\"" +  widgets.get(widget).get(0) + "\"";
                    } else if (type.equals("swift")) {
                        textAExecuter = "swift " + "\"" +  widgets.get(widget).get(0) + "\"";
                    } else if (type.equals("kt")) {
                        textAExecuter = "kotlinc -script " + "\"" +  widgets.get(widget).get(0) + "\"";
                    } else if (type.equals("rb")) {
                        textAExecuter = "ruby " + "\"" +  widgets.get(widget).get(0) + "\"";
                    } else if (type.equals("go")) {
                        textAExecuter = "go run " + "\"" +  widgets.get(widget).get(0) + "\"";
                    } else if (type.equals("rs")) {
                        String path = "";
                        for (int i = 0; i < pathDecompose.length - 1; i++) {
                            path += pathDecompose[i] + "\\";
                        }
                        String mot = "";
                        for (int i = 0; i < fichierDecompose.length - 1; i++) {
                            if (i == fichierDecompose.length - 2) {
                                mot += fichierDecompose[i];
                            } else {
                                mot += fichierDecompose[i] + ".";
                            }
                        }
                        textAExecuter = "rustc " + "\"" +  widgets.get(widget).get(0) + "\"" + "-o " + "\"" + path + mot + "\"" + " && "  + "\"" + path + mot + "\"";
                    } else if (type.equals("sh")) {
                        textAExecuter = "sh " + "\"" +  widgets.get(widget).get(0) + "\"";
                    } else if (type.equals("r")) {
                        textAExecuter = "Rscript " + "\"" +  widgets.get(widget).get(0) + "\"";
                    }
                    break;
                }
            }
            String[] executables = new String[]{textAExecuter};
            if (!(textAExecuter.equals(""))) {
                SwingUtilities.invokeLater(() ->  {
                    String textAExecuterLocal = executables[0];
                    String NewtextAjoute = textAExecuterLocal;
                    Document document = commandes.getDocument();
                    document.removeDocumentListener(DocListener);
                    try {
                        TextSauvegarde = commandes.getText();
                        document.insertString(document.getLength(), NewtextAjoute, input);
                        document.insertString(document.getLength(), "\n", defStyle);
                        colorJaune.add(new ArrayList<>(List.of(TextSauvegarde.length(), NewtextAjoute.length())));
                        communicateWithCMD(NewtextAjoute);
                    } catch (BadLocationException err) {
                        err.printStackTrace();
                    } 
                    document.addDocumentListener(DocListener);
                });
            }
        });
        bouton8.addActionListener(e -> {
            widgets.clear();
            fenetres.removeAll();
            fenetres.revalidate();
            fenetres.repaint();
        });
        bouton9.addActionListener(e -> {  
            String nomDossier = "nomActuelFichier.txt";
            File Dirfile = new File(pathInput.getText());
            if (Dirfile.exists() && Dirfile.isDirectory()) {
                for (File file : Dirfile.listFiles()) {
                    if (file.isFile()) {
                        nomDossier = file.getName();
                        break;
                    }
                }
            }
            Document document = commandes.getDocument();
            createFile = true;
            try {
                String AjoutTextAjoute = " ren " + "\""+ nomDossier +"\"" + " \"nouveauNomFichier.txt\"" + " & :: entrez l'ancien nom (ou le chemin) du fichier et son nouveau nom.";
                int length = commandes.getText().length();
                document.insertString(length, AjoutTextAjoute, input);
            } catch (BadLocationException err) {
                err.printStackTrace();
            }
            repeatRequest = false;
        });
        bouton10.addActionListener(e -> {
            String nomDossier = "DossierASupprimer";
            File Dirfile = new File(pathInput.getText());
            if (Dirfile.exists() && Dirfile.isDirectory()) {
                for (File file : Dirfile.listFiles()) {
                    if (file.isDirectory()) {
                        nomDossier = file.getName();
                        break;
                    }
                }
            }
            Document document = commandes.getDocument();
            createFile = true;
            try {
                String AjoutTextAjoute = " rmdir /S /Q " + nomDossier + " & :: vous pouvez modifier le nom (ou le chemin) du dossier a supprimer.";
                document.insertString(commandes.getText().length(), AjoutTextAjoute, input);
            } catch (BadLocationException err) {
                err.printStackTrace();
            }
            repeatRequest = false;
        });
    } 
}
