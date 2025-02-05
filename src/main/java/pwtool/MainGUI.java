package pwtool;

import com.github.terefang.jmelange.commons.CommonUtil;
import com.github.terefang.jmelange.commons.loader.ClasspathResourceLoader;
import com.github.terefang.jmelange.commons.util.ListMapUtil;
import com.github.terefang.jmelange.data.ldata.LdataParser;
import com.github.terefang.jmelange.passwd.PwTool;
import com.github.terefang.jmelange.passwd.crypt.BcryptFunction;
import com.github.terefang.jmelange.passwd.crypt.Md5Crypt;
import com.github.terefang.jmelange.passwd.crypt.Sha2Crypt;
import com.github.terefang.jmelange.passwd.util.PBKDF;
import com.github.terefang.jmelange.random.ArcRand;
import com.github.terefang.jmelange.swing.SwingHelper;
import com.jidesoft.plaf.LookAndFeelFactory;
import com.jidesoft.swing.JideBoxLayout;
import com.jidesoft.swing.JideButton;
import com.jidesoft.swing.JideComboBox;
import com.jidesoft.swing.JideSplitButton;
import io.github.dheid.fontchooser.FontDialog;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import me.gosimple.nbvcxz.Nbvcxz;
import me.gosimple.nbvcxz.scoring.Result;
import org.jfree.ui.FontChooserDialog;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.text.DefaultCaret;
import javax.swing.text.DefaultEditorKit;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.time.Duration;
import java.time.temporal.TemporalUnit;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;


@Slf4j
public class MainGUI extends JFrame
{
    private JPanel panel;

    private JTextArea _textArea;

    private JScrollPane _scroll;

    private JideComboBox _seed_box;
    private SpinnerNumberModel _pwlen_model;
    JSpinner _pwlen;
    private DefaultComboBoxModel _seed;
    private JTextField _set1;
    private JTextField _set2;
    private JTextField _set3;
    private JTextField _set4;
    private LinkedHashMap<String, String[]> _psets;
    private JCheckBox cbCard;
    private JCheckBox cbInfo;
    private JCheckBox cbCrypt;
    private JCheckBox cbHex;


    public MainGUI() throws HeadlessException
    {
        super("Pwtool "+Version._VERSION);
    }

    public static int _BHEIGHT = 30;


    @SneakyThrows
    public void init()
    {
        //this.setStartPosition(StartPosition.CenterInScreen);
        this.setSize(new Dimension(400,200));

        this.panel = new JPanel();
        this.panel.setLayout(new JideBoxLayout(this.panel, JideBoxLayout.Y_AXIS, 6));
        this.add(this.panel);

        createSeedGUI();
        createSets();

        createTabbedArea();

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(true);
        this.pack();
        this.setVisible(true);
    }

    private void createSeedGUI()
    {
        Border _etch = BorderFactory.createEtchedBorder((EtchedBorder.LOWERED));
        TitledBorder _title = BorderFactory.createTitledBorder(_etch, "Seed");

        JPanel _panel = new JPanel();
        _panel.setLayout(new BoxLayout(_panel, BoxLayout.X_AXIS));
        _panel.setBorder(_title);

        this._pwlen_model = new SpinnerNumberModel(16, 4, 64, 1);
        this._pwlen_model.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                MainGUI.this.writeHash();
            }
        });
        this._pwlen = new JSpinner(this._pwlen_model);
        this._pwlen.setMinimumSize(new Dimension(75, _BHEIGHT));
        this._pwlen.setMaximumSize(new Dimension(75, _BHEIGHT));
        this._pwlen.setSize(new Dimension(75, _BHEIGHT));
        this._pwlen.setFont(_guifont);
        _panel.add(this._pwlen);

        this._seed = new DefaultComboBoxModel();
        this._seed.addElement("SECRET");
        this._seed.setSelectedItem("SECRET");
        this._seed_box = new JideComboBox(this._seed);
        this._seed_box.setFont(_guifont);
        this._seed_box.setMinimumSize(new Dimension(256, _BHEIGHT));
        this._seed_box.setMaximumSize(new Dimension(256, _BHEIGHT));
        this._seed_box.setSize(new Dimension(256, _BHEIGHT));
        this._seed_box.setEditable(true);
        this._seed_box.addActionListener((x) -> {
            if("comboBoxEdited".equalsIgnoreCase(x.getActionCommand()))
            {
                String _s = this._seed_box.getEditor().getItem().toString();
                this._seed.addElement(_s);
                this._seed.setSelectedItem(_s);
            }
        });
        this._seed.addListDataListener(new ListDataListener() {
            @Override
            public void intervalAdded(ListDataEvent e) {
                MainGUI.this.writeHash();
            }

            @Override
            public void intervalRemoved(ListDataEvent e) {
                MainGUI.this.writeHash();
            }

            @Override
            public void contentsChanged(ListDataEvent e) {
                MainGUI.this.writeHash();
            }
        });
        _panel.add(this._seed_box);

        _panel.add(new JideButton(new AbstractAction("X") {
            @Override
            public void actionPerformed(ActionEvent e) {
                MainGUI.this._seed.removeElement(MainGUI.this._seed.getSelectedItem());
            }
        }));

        _panel.add(new JideButton(new AbstractAction("Random") {
            @Override
            public void actionPerformed(ActionEvent e) {
                String _r = UUID.randomUUID().toString();
                MainGUI.this._seed.addElement(_r);
                MainGUI.this._seed.setSelectedItem(_r);
            }
        }));

        JideSplitButton _btn = new JideSplitButton(new AbstractAction("Defaults") {
            @Override
            public void actionPerformed(ActionEvent e) {
                MainGUI.this._set1.setText("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
                MainGUI.this._set2.setText("abcdefghijklmnopqrstuvwxyz");
                MainGUI.this._set3.setText("0123456789");
                MainGUI.this._set4.setText("_!§$%&/=?#*+");
                MainGUI.this._seed.addElement("S3cr3T!");
                MainGUI.this._seed.setSelectedItem("S3cr3T!");
                MainGUI.this.writeHash();
            }
        });

        if(this._psets.size()>0)
        {
            _btn.add(new JSeparator());
        }

        for(final String _pset : this._psets.keySet())
        {
            _btn.add(new AbstractAction(_pset) {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    String[] _vset = MainGUI.this._psets.get(_pset);
                    if(_vset.length>0) { MainGUI.this._set1.setText(_vset[0]); } else { MainGUI.this._set1.setText(""); }
                    if(_vset.length>1) { MainGUI.this._set2.setText(_vset[1]); } else { MainGUI.this._set2.setText(""); }
                    if(_vset.length>2) { MainGUI.this._set3.setText(_vset[2]); } else { MainGUI.this._set3.setText(""); }
                    if(_vset.length>3) { MainGUI.this._set4.setText(_vset[3]); } else { MainGUI.this._set4.setText(""); }
                    MainGUI.this.writeHash();
                }
            });
        }
        _panel.add(_btn);

        _panel.add(new JLabel(" | "));
        _panel.add(this.cbCard = new JCheckBox("Card", false));
        _panel.add(this.cbInfo = new JCheckBox("Info", false));
        _panel.add(this.cbHex = new JCheckBox("Hex", false));
        _panel.add(this.cbCrypt = new JCheckBox("Crypt", false));
        
        _panel.add(new JLabel(" | "));

        _panel.add(new JideButton(new AbstractAction("A↑") {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                MainGUI.this.handleIncreaseFontSize();
            }
        }));
        _panel.add(new JideButton(new AbstractAction("F") {
            @Override
            public void actionPerformed(ActionEvent e) {
                MainGUI.this.handleSelectFont();
            }
        }));
        _panel.add(new JideButton(new AbstractAction("A↓") {
            @Override
            public void actionPerformed(ActionEvent e) {
                MainGUI.this.handleDecreaseFontSize();
            }
        }));
        _panel.add(new JLabel(" | "));
        _panel.add(SwingHelper.createButton("Refresh",()->{ this.writeHash(); }));
        
        this.panel.add(_panel);
    }

    private void handleSelectFont2() {
        SwingUtilities.invokeLater(() -> {
            FontDialog dialog = new FontDialog((Frame)null, "Select Font", true);
            dialog.setDefaultCloseOperation(2);
            dialog.setLocation(this.getLocation());
            dialog.setSelectedFont(this._textArea.getFont());
            dialog.setVisible(true);
            if (!dialog.isCancelSelected()) {
                this._textArea.setFont(dialog.getSelectedFont());
            }
            //CustomColorChooser.choose((colorSelectionEvent) -> {});
        });
    }

    private void handleSelectFont() {
        SwingUtilities.invokeLater(() -> {
            int _size = this._textArea.getFont().getSize();
            FontChooserDialog _fntd = new FontChooserDialog(new JFrame(""), "Choose Font", false, this._textArea.getFont())
            {
                @Override
                public void actionPerformed(ActionEvent event) {
                    super.actionPerformed(event);
                    String _cmd = event.getActionCommand();
                    if("okButton".equalsIgnoreCase(_cmd))
                    {
                        MainGUI.this._textArea.setFont(this.getSelectedFont());
                    }
                }
            };
            _fntd.setMinimumSize(new Dimension(300,300));
            _fntd.setLocation(MainGUI.this.getLocation());
            _fntd.setVisible(true);
        });
    }

    private void createSets()
    {
        Border _etch = BorderFactory.createEtchedBorder((EtchedBorder.LOWERED));
        TitledBorder _title = BorderFactory.createTitledBorder(_etch, "Sets");

        JPanel _panel = new JPanel();
        _panel.setLayout(new BoxLayout(_panel, BoxLayout.Y_AXIS));
        _panel.setBorder(_title);
        KeyListener _listener = new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {

            }

            @Override
            public void keyPressed(KeyEvent e) {

            }

            @Override
            public void keyReleased(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_ENTER) MainGUI.this.writeHash();
            }
        };
        this._set1 = new JTextField("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        this._set1.addKeyListener(_listener);
        this._set1.setFont(_guifont);
        this._set1.setMaximumSize(new Dimension(Integer.MAX_VALUE, _BHEIGHT));
        _panel.add(this._set1);
        this._set2 = new JTextField("abcdefghijklmnopqrstuvwxyz");
        this._set2.addKeyListener(_listener);
        this._set2.setFont(_guifont);
        this._set2.setMaximumSize(new Dimension(Integer.MAX_VALUE, _BHEIGHT));
        _panel.add(this._set2);
        this._set3 = new JTextField("0123456789");
        this._set3.addKeyListener(_listener);
        this._set3.setFont(_guifont);
        this._set3.setMaximumSize(new Dimension(Integer.MAX_VALUE, _BHEIGHT));
        _panel.add(this._set3);
        this._set4 = new JTextField("_!§$%&/=?#*+");
        this._set4.addKeyListener(_listener);
        this._set4.setFont(_guifont);
        this._set4.setMaximumSize(new Dimension(Integer.MAX_VALUE, _BHEIGHT));
        _panel.add(this._set4);

        this.panel.add(_panel);
    }

    public void handleIncreaseFontSize() {
        int _size = this._textArea.getFont().getSize();
        this._textArea.setFont(
                this._textArea.getFont().deriveFont((float)(2f+_size)));
    }

    public void handleDecreaseFontSize() {
        int _size = this._textArea.getFont().getSize();
        this._textArea.setFont(
                this._textArea.getFont().deriveFont((float)(_size-2f)));
    }

    @SneakyThrows
    private void writeHash()
    {
        SwingUtilities.invokeLater(() -> {

            String _seed = Objects.toString(this._seed.getSelectedItem(), "");
            this.logClear();
            int _L = 10;
            int _N = 26;

            List<String> _set = ListMapUtil.toList(
                    MainGUI.this._set1.getText(),
                    MainGUI.this._set2.getText(),
                    MainGUI.this._set3.getText(),
                    MainGUI.this._set4.getText());
            
            Nbvcxz nbvcxz = new Nbvcxz();
            Result _rs = nbvcxz.estimate(_seed);
            {
                double _time = Math.pow(2., _rs.getEntropy()-log(1000., 2.));
                this.logPrint(String.format("%-14s | %s (%d; %.2f; %s) \n", "PLAINTEXT",_seed, _rs.getBasicScore(), _rs.getEntropy(), humanReadableFormat((long) _time)));
            }

            
            if(this.cbCard.isSelected())
            {
                String _pwc = generatePassword(_seed,_L*_N);
                this.logPrint("-:");
                for(int _j =0; _j< _N; _j++)
                {
                    this.logPrint(String.format("%s",Character.toString((char) ('A'+_j))));
                }
                this.logPrint("\n");
                int _i = 0;
                for(int _j =0; _j< _pwc.length(); _j+=_N)
                {
                    this.logPrint(String.format("%d:%s\n",_i,_pwc.substring(_j,_j+_N)));
                    _i++;
                }

                this.logPrint("\n");
            }

            nbvcxz = new Nbvcxz();
            for(PwTool.DigestHash _algo : PwTool.DigestHash.values())
            {
                this.logPrint(String.format("%-14s", _algo));
                String _pw = PwTool.passwordToPassword(_algo,_seed,_set,this._pwlen_model.getNumber().intValue());
                _rs = nbvcxz.estimate(_pw);
                if(!this.cbInfo.isSelected())
                {
                    double _time = Math.pow(2., _rs.getEntropy()-log(1000., 2.));
                    this.logPrint(String.format(" | %s (%d; %.2f; %s) \n", _pw, _rs.getBasicScore(), _rs.getEntropy(), humanReadableFormat((long) _time)));
                }
                else
                {
                    this.logPrint(String.format(" | %s (%d, %.2f) \n", _pw, _rs.getBasicScore(), _rs.getEntropy()));
                    double _time = Math.pow(2., _rs.getEntropy()-log(12500., 2.));
                    this.logPrint(String.format("Time to Crack (12k5 O/s) -> %s \n", humanReadableFormat((long) _time)));
                    _time = Math.pow(2., _rs.getEntropy()-log(1000., 2.));
                    this.logPrint(String.format("Time to Crack (1k R/s) -> %s \n", humanReadableFormat((long) _time)));
                }

            }

            if(this.cbInfo.isSelected())
            {
                this.logPrint("\nScores:\n");
                this.logPrint("  0: risky password: 'too guessable'\n");
                this.logPrint("  1: modest protection from throttled online attacks: 'very guessable'\n");
                this.logPrint("  2: modest protection from unthrottled online attacks: 'somewhat guessable'\n");
                this.logPrint("  3: modest protection from offline attacks: 'safely unguessable'\n      (assuming a salted, slow hash function)\n");
                this.logPrint("  4: strong protection from offline attacks: 'very unguessable'\n      Di(assuming a salted, slow hash function)\n\n");
            }

            if(this.cbCrypt.isSelected())
            {
                this.logPrint(String.format("%-14s", "bCrypt"));
                this.logPrint(String.format(" | %s \n", BcryptFunction.generate(_seed)));
                
                this.logPrint(String.format("%-14s", "md5Crypt"));
                this.logPrint(String.format(" | %s \n", Md5Crypt.md5Crypt(_seed.getBytes(StandardCharsets.UTF_8))));
                
                this.logPrint(String.format("%-14s", "secret5"));
                this.logPrint(String.format(" | %s \n", Md5Crypt.ciscoMd5Crypt(_seed)));
                
                this.logPrint(String.format("%-14s", "aprCrypt"));
                this.logPrint(String.format(" | %s \n", Md5Crypt.apr1Crypt(_seed)));

                this.logPrint(String.format("%-14s", "sha256Crypt"));
                this.logPrint(String.format(" | %s \n", Sha2Crypt.sha256Crypt(_seed.getBytes(StandardCharsets.UTF_8))));

                this.logPrint(String.format("%-14s", "sha512Crypt"));
                this.logPrint(String.format(" | %s \n", Sha2Crypt.sha512Crypt(_seed.getBytes(StandardCharsets.UTF_8))));
            }

        });
    }

    public static double log(double value, double base) {
        return Math.log(value)/Math.log(base);
    }

    public static String humanReadableFormat(long _s)
    {
        Duration duration = Duration.ofSeconds(_s);
        StringBuilder _sb = new StringBuilder();
        if(duration.toDaysPart()>0)
        {
            long _d = duration.toDaysPart();
            long _y = _d/365L;
            _d= _d-(_y*365L);
            if(_y>1000000)
            {
                _sb.append((_y/1000000)+"My ");
            }
            else if(_y>1000)
            {
                _sb.append((_y/1000)+"ky ");
            }
            else if(_y>0)
            {
                _sb.append(_y+"y ");
            }
            _sb.append(_d+"d ");
        }

        if(duration.toHoursPart()>0)
        {
            _sb.append(duration.toHoursPart()+"h ");
        }

        if(duration.toMinutesPart()>0)
        {
            _sb.append(duration.toMinutesPart()+"m ");
        }

        if(duration.toSecondsPart()>0)
        {
            _sb.append(duration.toSecondsPart()+"s ");
        }

        return _sb.toString().trim();
    }

    private void createTabbedArea()
    {
        this._textArea = new JTextArea(20, 72);
        this._textArea.setEditable(false);
        DefaultCaret caret = (DefaultCaret) this._textArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        this._textArea.setForeground(Color.WHITE);
        this._textArea.setBackground(Color.BLACK);
        this._textArea.setFont(createEditFont());

        this._textArea.addMouseListener(new MouseAdapter()
        {
            public void mouseReleased(final MouseEvent e)
            {
                if (SwingUtilities.isRightMouseButton(e))
                {
                    final JTextArea component = (JTextArea)e.getComponent();
                    final JPopupMenu menu = new JPopupMenu();
                    JMenuItem item = new JMenuItem(new DefaultEditorKit.CopyAction());
                    item.setText("Copy");
                    item.setEnabled(component.getSelectionStart() != component.getSelectionEnd());
                    menu.add(item);
                    item = new JMenuItem(new AbstractAction() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            MainGUI.this._textArea.setText("");
                        }
                    });
                    item.setText("Clear");
                    menu.add(item);
                    menu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });

        this._scroll = new JScrollPane(this._textArea);
        this._scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        this.panel.add(this._scroll);
    }

    private void logClear() {
        this._textArea.setText("");
        this._textArea.repaint();
    }

    public synchronized void logPrint(String message)
    {
        this._textArea.append(message);
        this._textArea.setCaretPosition(this._textArea.getDocument().getLength());
        this._textArea.repaint();
    }

    @SneakyThrows
    public static void main(String[] args)
    {
        if(OsUtil.isMac)
        {
            System.err.println("Setting MacOS L&F ...");
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            System.setProperty("swing.defaultlaf", UIManager.getCrossPlatformLookAndFeelClassName());
            LookAndFeelFactory.installDefaultLookAndFeelAndExtension();
        }

        final MainGUI _main = new MainGUI();

        String[] _clist = new String[] {
                OsUtil.getUserConfigDirectory("pwtool"),
                OsUtil.getUserDataDirectory("pwtool"),
                OsUtil.getUserConfigDirectory(),
                OsUtil.getUserDataDirectory(),
                OsUtil.getJarDirectory(),
                OsUtil.getCurrentDirectory()
        };

        _main._psets = new LinkedHashMap<String, String[]>();
        for(String _cpath : _clist)
        {
            File _cfile = new File(_cpath, "pwtool.pdata");
            _main.tryAddData(_cfile);
        }

        _main.tryAddData(new File(OsUtil.getJarPath()+".pdata"));

        _main.init();
        _main.pack();
        _main.setVisible(true);
    }

    void tryAddData(File _cfile)
    {
        if(_cfile.exists())
        {
            System.err.println("found: "+_cfile.getAbsolutePath());
            for(Map.Entry<String, Object> _entry : LdataParser.loadFrom(_cfile).entrySet())
            {
                this._psets.put(_entry.getKey(), ((List<String>)_entry.getValue()).toArray(new String[0]));
            }
        }
        else
        {
            System.err.println("does not exist: "+_cfile.getAbsolutePath());
        }
    }

    List<char[]> getCherList()
    {
        char[] _set_1 = this._set1.getText().toCharArray();
        char[] _set_2 = this._set2.getText().toCharArray();
        char[] _set_3 = this._set3.getText().toCharArray();
        char[] _set_4 = this._set4.getText().toCharArray();

        List<char[]> _set_list = new Vector<>();
        if(_set_1.length>0)
        {
            _set_list.add(_set_1);
        }
        if(_set_2.length>0)
        {
            _set_list.add(_set_2);
        }
        if(_set_3.length>0)
        {
            _set_list.add(_set_3);
        }
        if(_set_4.length>0)
        {
            _set_list.add(_set_4);
        }

        return _set_list;
    }

    public String generatePassword(String _seed, int _len)
    {
        ArcRand _rng = ArcRand.from(_seed);

        int _row = 16;

        for(char[] _set : getCherList())
        {
            if(_row<_set.length) _row = _set.length;
        }

        StringBuilder _sb = new StringBuilder();
        for(char[] _set : getCherList())
        {
            ArcRand _r = ArcRand.from(_rng.next32(), _row);

            for(int _i : _r._ctx)
            {
                _sb.append(_set[_i%_set.length]);
            }
        }

        _rng = ArcRand.from(_seed, _sb.length());
        StringBuilder _sb2 = new StringBuilder();
        char _last = 0;
        for(int _j =0; _j< _len; _j++)
        {
            char _that = _sb.charAt(_rng._ctx[_j%_rng._ctx.length]);
            if((_j>0) && (_j%_rng._ctx.length)==(_rng._ctx.length-1))
            {
                for(int _i =0; _i< _rng._ctx.length; _i++)
                    _rng.next32();
            }
            if(_last == _that)
            {
                _len++;
                continue;
            }
            _sb2.append(_that);
            _last = _that;
        }
        return _sb2.toString();
    }

    static Font _guifont = createEditFont();
    static Font _editfont = null;
    @SneakyThrows
    static public synchronized Font createEditFont(float _size)
    {
        if(_editfont==null)
        {
            _editfont = Font.createFont(Font.TRUETYPE_FONT,
                    ClasspathResourceLoader.of("JetBrainsMono-Regular.ttf", null).getInputStream())
            ;
        }
        return _editfont.deriveFont(_size);
    }
    @SneakyThrows
    static public synchronized Font createEditFont()
    {
        return createEditFont(16f);
    }

    static Font _regfont = null;
    static Font _boldfont = null;
    static Font _italicfont = null;
    static Font _bolditalicfont = null;
    static Map<String,Font> _font = new HashMap<>();


    @SneakyThrows
    static public synchronized Font createFont(String _file, float _size)
    {
        String _key = String.format("%s-0", _file);
        if(_font.containsKey(_key))
        {
            Font _ffont = Font.createFont(Font.TRUETYPE_FONT,
                    ClasspathResourceLoader.of(_file, null).getInputStream());
            _font.put(_key, _ffont);
        }
        return _font.get(_key).deriveFont(_size);

    }

    @SneakyThrows
    static public synchronized Font createDefaultFont(String[] _families, boolean _bold, boolean _italic, float _size)
    {
        String _key = String.format("%s-%s-%s-%d", _families[0], _bold, _italic, (int) _size);

        if (_font.containsKey(_key)) {
            return _font.get(_key);
        }

        if ("monospace".equalsIgnoreCase(_families[0]))
        {
            return createEditFont().deriveFont(_size);
        }

        if(_bold)
        {
            if(_italic)
            {
                if(_bolditalicfont==null)
                {
                    _bolditalicfont = Font.createFont(Font.TRUETYPE_FONT,
                            ClasspathResourceLoader.of("NunitoSans-BoldItalic.ttf", null).getInputStream());
                }

                _font.put(_key, _bolditalicfont.deriveFont(_size));
            }
            else
            {
                if(_boldfont==null)
                {
                    _boldfont = Font.createFont(Font.TRUETYPE_FONT,
                            ClasspathResourceLoader.of("NunitoSans-Bold.ttf", null).getInputStream());
                }
                _font.put(_key, _boldfont.deriveFont(_size));
            }
        }
        else
        {
            if(_italic)
            {
                if(_italicfont==null)
                {
                    _italicfont = Font.createFont(Font.TRUETYPE_FONT,
                            ClasspathResourceLoader.of("NunitoSans-Italic.ttf", null).getInputStream());
                }
                _font.put(_key,  _italicfont.deriveFont(_size));
            }
            else
            {
                if(_regfont==null)
                {
                    _regfont = Font.createFont(Font.TRUETYPE_FONT,
                            ClasspathResourceLoader.of("NunitoSans-Regular.ttf", null).getInputStream());
                }
                _font.put(_key, _regfont.deriveFont(_size));
            }
        }
        return _font.get(_key);
    }

    int checkPw(String _pw)
    {
        int _score = 0; // _pw.length()/2;

        /*
        Bonus points for length of the password:
        - 3 points 1 to 4 char;
        - 6 points 5 to 7 char;
        - 12 points 8 to 15 char;
        - 18 points 16 to 20 char;
        */

        if(_pw.length()>=16) { _score+=18; }
        else if(_pw.length()>=8) { _score+=12; }
        else if(_pw.length()>=5) { _score+=6;  }
        else if(_pw.length()>=1) { _score+=3; }

        int _low = 0;
        int _high = 0;
        int _num = 0;
        int _spec = 0;

        for(char _c : _pw.toCharArray())
        {
            if(_c>='a' && _c<='z')  { _low++; }
            else if(_c>='A' && _c<='Z') { _high++; }
            else if(_c>='0' && _c<='9') { _num++; }
            else  { _spec++; }
        }
        /*
        Weitere Informationen:
        The password's level of security is calculated in the following way:

            - 1 point for at least one lower case char;
        - 5 points per at least one upper case char;
        - 5 points per at least 1 cipher;
        - 5 points per at least 3 ciphers;
        - 5 points per at least 1 special char;
        - 5 points per at least 2 special chars;
        - 2 points for having used both lower and upper cases;
        - 2 points for having used both letters and numbers;
        - 2 points for having used letters, numbers and special characters;

        Please choose a password with at least 30 of max 50 points.
        */

        if(_low>=1) _score+=1;
        if(_high>=1) _score+=5;

        if(_spec>=2) { _score+=5; }
        if(_spec>=1) { _score+=5; }

        if(_num>=3) { _score+=5; }
        if(_num>=1) { _score+=5; }

        if(_low>0 && _high>0) { _score+=2; }

        if((_low>0 || _high>0) && _num>0) { _score+=2; }

        if((_low>0 || _high>0) && _num>0 && _spec>0) { _score+=2; }

        return _score;
    }

}
