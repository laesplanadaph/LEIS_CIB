package my.gui;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author gene chester
 */
public class MainFrame extends javax.swing.JFrame {
    
    DefaultTableModel tableModel;
    
    public MainFrame() {
        initComponents();
    }
    
    private Connection getConnection() {
        try {
            Connection conn = null;
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/leis","root","k3k4mj33");
            
            if(conn != null) {
                System.out.println("successfully connected to db!");
            }
            
            return conn;
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }
    
    private void preloadComboBox() {
        
        roomNoCB.addItem("Rm.");
        for (int i = 1;i<=7;i++) {
            roomNoCB.addItem(Integer.toString(i));
            
        }
 
        checkInMCB.addItem("MM");
        checkOutMCB.addItem("MM");
        for (int i = 1;i<=12;i++) {
            checkInMCB.addItem(Integer.toString(i));
            checkOutMCB.addItem(Integer.toString(i));
        }
        
        checkInDCB.addItem("DD");
        checkOutDCB.addItem("DD");
        for (int i = 1; i <= 31;i++) {
            checkInDCB.addItem(Integer.toString(i));
            checkOutDCB.addItem(Integer.toString(i));
        }
        
        DateFormat year = new SimpleDateFormat("yyyy");
        Date date = new Date();
        int currentYear = Integer.parseInt(year.format(date));
        
        checkInYCB.addItem("YYYY");
        checkOutYCB.addItem("YYYY");
        for (int i = currentYear; i <= currentYear+10;i++) {
            checkInYCB.addItem(Integer.toString(i));
            checkOutYCB.addItem(Integer.toString(i));
        }
        
    }
    
    private ArrayList<Reservation> getReservationList() {
        
        ArrayList<Reservation> reservationList = new ArrayList<Reservation>();
            
        Connection conn = getConnection();

        String query = "SELECT * FROM reservation";
        Statement st;
        ResultSet rs;

        try {
            st = conn.createStatement();
            rs = st.executeQuery(query);

            Reservation reservation;

            while(rs.next()){
                reservation = new Reservation(rs.getInt("reservation_id"),rs.getString("name"),rs.getInt("room"),rs.getString("check_in"),rs.getString("check_out"),rs.getString("date_added"));
                reservationList.add(reservation);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
            return reservationList;
    }
    
    private void displayReservationList(){
        try {
            ArrayList<Reservation> list= getReservationList();
            
            tableModel = (DefaultTableModel) schedTable.getModel(); 
            
            DateFormat dateAddedFormat = new SimpleDateFormat("MM-dd-yyyy hh:mm a");
            DateFormat checkInOutFormat = new SimpleDateFormat("MM-dd-yyyy");
            DateFormat dateAddedParse = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            DateFormat checkInOutParse = new SimpleDateFormat("yyyy-MM-dd");
            
            Object[] row = new Object[6];
            
            for (int i = 0; i < list.size(); i++) {
                row[0] = String.format("%06d", list.get(i).getId());
                row[1] = list.get(i).getName();
                row[2] = list.get(i).getRoomNo();
                row[3] = checkInOutFormat.format(checkInOutParse.parse(list.get(i).getCheckIn()));
                row[4] = checkInOutFormat.format(checkInOutParse.parse(list.get(i).getCheckOut()));
                row[5] = dateAddedFormat.format(dateAddedParse.parse(list.get(i).getDateAdded()));
                
                tableModel.addRow(row);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }  
    }
    
    private boolean validateForm(){
        if (!nameTF.getText().trim().isEmpty() && 
                roomNoCB.getSelectedItem() != "Rm." &&
                checkInMCB.getSelectedItem() != "MM" &&
                checkInDCB.getSelectedItem() != "DD" &&
                checkInYCB.getSelectedItem() != "YYYY" &&
                checkOutMCB.getSelectedItem() != "MM" &&
                checkOutDCB.getSelectedItem() != "DD" &&
                checkOutYCB.getSelectedItem() != "YYYY" &&
                validateDates()){
            return true;
        } else {
            return false;
        }
    }
    
    private boolean validateDates() {
        int checkInM = Integer.parseInt((String) checkInMCB.getSelectedItem());
        int checkInD = Integer.parseInt((String) checkInDCB.getSelectedItem());
        int checkInY = Integer.parseInt((String) checkInYCB.getSelectedItem());
        int checkOutM = Integer.parseInt((String) checkOutMCB.getSelectedItem());
        int checkOutD = Integer.parseInt((String) checkOutDCB.getSelectedItem());
        int checkOutY = Integer.parseInt((String) checkOutYCB.getSelectedItem());
        
        boolean result = false;

        if (checkOutY == checkInY){
            if (checkOutM == checkInM) {
                if (checkOutD > checkInD) {
                    result = true;
                }
            } else if (checkOutM > checkInM) {
                result = true;
            }
        } else if (checkOutY > checkInY) {
            result = true;
        }
        
        return result;
    }
    
    private DateConflict dateConflictChecker() {
        DateConflict result = new DateConflict(true, true);
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String checkInForm;
            String checkOutForm;
            Date checkInDateForm;
            Date checkOutDateForm;
            
            if(checkInYCB.equals("YYYY") || checkOutYCB.equals("YYYY") ||  
               checkInMCB.equals("MM") || checkOutMCB.equals("MM") ||
               checkInMCB.equals("DD") || checkOutMCB.equals("DD")) {
            
                checkInForm = checkInYCB.getSelectedItem() +"-"+ checkInMCB.getSelectedItem() +"-"+ checkInDCB.getSelectedItem();
                checkOutForm = checkOutYCB.getSelectedItem() +"-"+ checkOutMCB.getSelectedItem() +"-"+ checkOutDCB.getSelectedItem();
                checkInDateForm = sdf.parse(checkInForm);
                checkOutDateForm = sdf.parse(checkOutForm);
            
                ArrayList<Reservation> datesWithoutId = getDates(Integer.parseInt(roomNoCB.getSelectedItem().toString()));
                ArrayList<Reservation> datesWithId = getDates(Integer.parseInt(roomNoCB.getSelectedItem().toString()),Integer.parseInt(idTF.getText()));

                if(datesWithoutId.isEmpty() || datesWithId.isEmpty()) {
                    return result;
                }

                for (int i = 0; i < datesWithoutId.size(); i++) {
                    int id = datesWithoutId.get(i).getId();
                    String checkIn = datesWithoutId.get(i).getCheckIn();
                    String checkOut = datesWithoutId.get(i).getCheckOut();
                    Date checkInDate = sdf.parse(checkIn);
                    Date checkOutDate = sdf.parse(checkOut);

                    if (checkInDateForm.compareTo(checkInDate) * checkOutDateForm.compareTo(checkOutDate) == 0 ||
                        (checkInDateForm.compareTo(checkInDate) < 0 && checkInDate.compareTo(checkOutDateForm) < 0) ||
                        (checkInDateForm.compareTo(checkInDate) > 0 && checkOutDate.compareTo(checkInDateForm) > 0)) {

                        result.setResultWithoutId(false);
                        break;     
                    }
                }

                for (int i = 0; i < datesWithId.size(); i++) {
                    int id = datesWithId.get(i).getId();
                    String checkIn = datesWithId.get(i).getCheckIn();
                    String checkOut = datesWithId.get(i).getCheckOut();
                    Date checkInDate = sdf.parse(checkIn);
                    Date checkOutDate = sdf.parse(checkOut);

                    if (checkInDateForm.compareTo(checkInDate) * checkOutDateForm.compareTo(checkOutDate) == 0 ||
                        (checkInDateForm.compareTo(checkInDate) < 0 && checkInDate.compareTo(checkOutDateForm) < 0) ||
                        (checkInDateForm.compareTo(checkInDate) > 0 && checkOutDate.compareTo(checkInDateForm) > 0)) {

                        result.setResultWithId(false);
                        break;     
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return result;
    }
    
    private ArrayList<Reservation> getDates(int room, int id){ //reservation_id currently not used (check for what it's conflicting with)
        Reservation dates;
        ArrayList<Reservation> dateList = new ArrayList<Reservation>();
        
        
        Connection conn = getConnection();
        
        String query = "SELECT reservation_id,check_in,check_out FROM reservation WHERE room='"+room+"' AND reservation_id <> '"+id+"'";
        Statement st;
        ResultSet rs;

        try {
            st = conn.createStatement();
            rs = st.executeQuery(query);
            
            while(rs.next()) {
                dates = new Reservation(rs.getInt("reservation_id"),rs.getString("check_in"),rs.getString("check_out"));
                dateList.add(dates);
            }
            

        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return dateList;
    }
    
    private ArrayList<Reservation> getDates(int room){ //reservation_id currently not used (check for what it's conflicting with)
        Reservation dates;
        ArrayList<Reservation> dateList = new ArrayList<Reservation>();
        
        
        Connection conn = getConnection();
        
        String query = "SELECT reservation_id,check_in,check_out FROM reservation WHERE room='"+room+"'";
        Statement st;
        ResultSet rs;

        try {
            st = conn.createStatement();
            rs = st.executeQuery(query);
            
            while(rs.next()) {
                dates = new Reservation(rs.getInt("reservation_id"),rs.getString("check_in"),rs.getString("check_out"));
                dateList.add(dates);
            }
            

        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return dateList;
    }
    
    private void dateValidation(String str) {
        String[] thirtyOne = {"MM","1","3","5","7","8","10","12"};
        String[] thirty = {"4","6","9","11"};
        
        JComboBox<String> d = new JComboBox<String>();
        JComboBox<String> m = new JComboBox<String>();
        JComboBox<String> y = new JComboBox<String>();
        
        if ("in".equals(str)){
            d = checkInDCB;
            m = checkInMCB;
            y = checkInYCB;            
        } else if ("out".equals(str)){
            d = checkOutDCB;
            m = checkOutMCB;
            y = checkOutYCB; 
        }  
        
        Object temp = d.getSelectedItem();
        
        if (m.getSelectedItem().equals("2")) {
            d.removeAllItems();
            d.addItem("DD");
            int feb = 29;
            
            if (y.getSelectedItem() != "YYYY") {
                if ( ( (Integer.parseInt( (String) y.getSelectedItem())) % 4 ) != 0) {
                    feb--;
                }
            }
            for (int i = 1 ; i <= feb ; i++) {
                    d.addItem(Integer.toString(i));
            }
        } else {
            d.removeAllItems();
            d.addItem("DD");
            for (String s : thirtyOne) {
                if (m.getSelectedItem().equals(s)){
                    for (int i = 1 ; i <= 31 ; i++) {
                        d.addItem(Integer.toString(i));
                    }
                    break;
                }
            }
            for (String s : thirty) {
                if (m.getSelectedItem().equals(s)) {
                    for (int i = 1 ; i <= 30 ; i++) {
                        d.addItem(Integer.toString(i));
                    }
                    break;
                }
            }
        }
        d.setSelectedItem(temp);
    }
    
    private Reservation getForm() {
        DateFormat dateAddedFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
   
        Reservation reservation;
                
        int id;
        String idStr = (String) idTF.getText();
        String name = nameTF.getText();
        int roomNo = Integer.parseInt((String) roomNoCB.getSelectedItem());
        String checkIn = checkInYCB.getSelectedItem() +"-"+ checkInMCB.getSelectedItem() +"-"+ checkInDCB.getSelectedItem();
        String checkOut = checkOutYCB.getSelectedItem() +"-"+ checkOutMCB.getSelectedItem() +"-"+ checkOutDCB.getSelectedItem();
        String dateTime = dateAddedFormat.format(date);
        
        if (!idStr.isEmpty()) {
            id = Integer.parseInt(idStr);
            reservation = new Reservation(id,name,roomNo,checkIn,checkOut,dateTime);
        } else {
            reservation = new Reservation(name,roomNo,checkIn,checkOut,dateTime);
        }
        return reservation;
    }

    private void setForm(int index) {
        
        String id = schedTable.getValueAt(index,0).toString();
        String name = schedTable.getValueAt(index,1).toString();
        String roomNo = schedTable.getValueAt(index,2).toString();
        String checkIn = schedTable.getValueAt(index,3).toString();
        String checkOut = schedTable.getValueAt(index,4).toString();
        
        idTF.setText(id);
        nameTF.setText(name);
        roomNoCB.setSelectedItem(roomNo);
        
        String[] cI = checkIn.split("-");
        checkInMCB.setSelectedItem(cI[0].replaceFirst("^0+(?!$)",""));
        checkInDCB.setSelectedItem(cI[1].replaceFirst("^0+(?!$)",""));
        checkInYCB.setSelectedItem(cI[2]);
        
        String[] cO = checkOut.split("-");
        checkOutMCB.setSelectedItem(cO[0].replaceFirst("^0+(?!$)",""));
        checkOutDCB.setSelectedItem(cO[1].replaceFirst("^0+(?!$)",""));
        checkOutYCB.setSelectedItem(cO[2]);
        
        addBtn.setEnabled(false);
    }
        
    private void clearForm() {
        nameTF.setText(null);
        roomNoCB.setSelectedIndex(0);
        checkInMCB.setSelectedIndex(0);
        checkInDCB.setSelectedIndex(0);
        checkInYCB.setSelectedIndex(0);
        checkOutMCB.setSelectedIndex(0);
        checkOutDCB.setSelectedIndex(0);
        checkOutYCB.setSelectedIndex(0);
        addBtn.setEnabled(false);
        editBtn.setEnabled(false);
    }
    
    private void insertData(Reservation r) {
        Connection conn = getConnection();

        String query = "INSERT INTO reservation (name,room,check_in,check_out,date_added) VALUES ('"+
                r.getName() +"','"+
                r.getRoomNo() +"','"+
                r.getCheckIn() +"','"+
                r.getCheckOut() +"','"+
                r.getDateAdded() +"')";
        Statement st;
        int rs;

        try {
            st = conn.createStatement();
            rs = st.executeUpdate(query);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void deleteData(int id) {
        Connection conn = getConnection();
        
        String query = "DELETE FROM reservation WHERE reservation_id='"+id+"'";
        Statement st;
        int rs;

        try {
            st = conn.createStatement();
            rs = st.executeUpdate(query);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void updateData(Reservation r){
        Connection conn = getConnection();
        System.out.println("updateData Connection complete");
        String query = "UPDATE reservation "
                +"SET name='"+r.getName() 
                +"',room='"+r.getRoomNo() 
                +"',check_in='"+ r.getCheckIn() 
                +"',check_out='"+ r.getCheckOut()
                +"' WHERE reservation_id='"+r.getId()+"'";
        Statement st;
        int rs;
        System.out.println(query);
        try {
            st = conn.createStatement();
            rs = st.executeUpdate(query);
            System.out.println("query complete");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void buttonControlEditDelete() {
        if  (schedTable.getSelectedRowCount() == 0) {
            deleteBtn.setEnabled(false);
            editBtn.setEnabled(false);
        } else if (schedTable.getSelectedRowCount() == 1) {
            deleteBtn.setEnabled(true);
            editBtn.setEnabled(true);
        } else if (schedTable.getSelectedRowCount() > 1) {
            deleteBtn.setEnabled(true);
            editBtn.setEnabled(false);
        }
    }
    
    private void buttonControlAddEdit() {
        boolean add = dateConflictChecker().getResultWithoutId();
        boolean edit = dateConflictChecker().getResultWithoutId();
        
        if (validateForm()) {
            if (add)
                addBtn.setEnabled(true);
            else
                addBtn.setEnabled(false);
            
            if (schedTable.getSelectedRowCount() == 1 ){
                if (edit)
                    editBtn.setEnabled(true);
                else
                    editBtn.setEnabled(false);
            }

        } else {
            addBtn.setEnabled(false);
            editBtn.setEnabled(false);
        }
    }
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        table_panel = new javax.swing.JPanel();
        addBtn = new javax.swing.JButton();
        exitBtn = new javax.swing.JButton();
        deleteBtn = new javax.swing.JButton();
        editBtn = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        schedTable = new javax.swing.JTable();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        nameTF = new javax.swing.JTextField();
        statusLabel = new javax.swing.JLabel();
        clearBtn = new javax.swing.JButton();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        roomNoCB = new javax.swing.JComboBox<>();
        deselectBtn = new javax.swing.JButton();
        checkInMCB = new javax.swing.JComboBox<>();
        checkOutMCB = new javax.swing.JComboBox<>();
        checkInDCB = new javax.swing.JComboBox<>();
        checkOutDCB = new javax.swing.JComboBox<>();
        checkInYCB = new javax.swing.JComboBox<>();
        checkOutYCB = new javax.swing.JComboBox<>();
        idTF = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setMaximumSize(new java.awt.Dimension(1275, 500));
        setMinimumSize(new java.awt.Dimension(1275, 500));
        setPreferredSize(new java.awt.Dimension(1023, 600));
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });

        table_panel.setBorder(javax.swing.BorderFactory.createTitledBorder("La Esplanada Scheduling System"));

        addBtn.setText("Add");
        addBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addBtnActionPerformed(evt);
            }
        });

        exitBtn.setText("Exit");
        exitBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitBtnActionPerformed(evt);
            }
        });

        deleteBtn.setText("Delete");
        deleteBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteBtnActionPerformed(evt);
            }
        });

        editBtn.setText("Edit");
        editBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editBtnActionPerformed(evt);
            }
        });

        schedTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID No.", "Name", "Room No.", "Check-in Date", "Check-out Date", "Date Added"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        schedTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                schedTableMouseClicked(evt);
                mouseEditDeleteButtonControl(evt);
                mouseAddEditButtonControl(evt);
            }
        });
        schedTable.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                keyboardEditDeleteButtonControl(evt);
                schedTableKeyboardReleased(evt);
                keyboardAddEditButtonControl(evt);
            }
        });
        jScrollPane1.setViewportView(schedTable);
        if (schedTable.getColumnModel().getColumnCount() > 0) {
            schedTable.getColumnModel().getColumn(0).setResizable(false);
            schedTable.getColumnModel().getColumn(1).setResizable(false);
            schedTable.getColumnModel().getColumn(2).setResizable(false);
            schedTable.getColumnModel().getColumn(3).setResizable(false);
            schedTable.getColumnModel().getColumn(4).setResizable(false);
            schedTable.getColumnModel().getColumn(5).setResizable(false);
        }

        jLabel1.setText("Name:");

        jLabel2.setText("Room #:");

        jLabel3.setText("Check-in Date:");

        jLabel4.setText("Check-out Date:");

        nameTF.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                keyboardAddEditButtonControl(evt);
            }
        });

        clearBtn.setText("Clear");
        clearBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearBtnActionPerformed(evt);
            }
        });

        jLabel7.setText("-");

        jLabel8.setText("-");

        jLabel9.setText("-");

        jLabel10.setText("-");

        roomNoCB.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] {}));
        roomNoCB.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            public void popupMenuCanceled(javax.swing.event.PopupMenuEvent evt) {
            }
            public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {
                popUpInvisibleAddEditButtonControl(evt);
            }
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {
            }
        });

        deselectBtn.setText("Deselect");
        deselectBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deselectBtnActionPerformed(evt);
            }
        });

        checkInMCB.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] {}));
        checkInMCB.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            public void popupMenuCanceled(javax.swing.event.PopupMenuEvent evt) {
            }
            public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {
                checkInMCBPopupMenuWillBecomeInvisible(evt);
                popUpInvisibleAddEditButtonControl(evt);
            }
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {
            }
        });

        checkOutMCB.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] {}));
        checkOutMCB.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            public void popupMenuCanceled(javax.swing.event.PopupMenuEvent evt) {
            }
            public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {
                checkOutMCBPopupMenuWillBecomeInvisible(evt);
                popUpInvisibleAddEditButtonControl(evt);
            }
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {
            }
        });

        checkInDCB.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] {}));
        checkInDCB.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            public void popupMenuCanceled(javax.swing.event.PopupMenuEvent evt) {
            }
            public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {
                popUpInvisibleAddEditButtonControl(evt);
            }
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {
            }
        });

        checkOutDCB.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] {}));
        checkOutDCB.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            public void popupMenuCanceled(javax.swing.event.PopupMenuEvent evt) {
            }
            public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {
                popUpInvisibleAddEditButtonControl(evt);
            }
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {
            }
        });

        checkInYCB.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] {}));
        checkInYCB.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            public void popupMenuCanceled(javax.swing.event.PopupMenuEvent evt) {
            }
            public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {
                checkInMCBPopupMenuWillBecomeInvisible(evt);
                popUpInvisibleAddEditButtonControl(evt);
            }
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {
            }
        });

        checkOutYCB.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] {}));
        checkOutYCB.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            public void popupMenuCanceled(javax.swing.event.PopupMenuEvent evt) {
            }
            public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {
                checkOutMCBPopupMenuWillBecomeInvisible(evt);
                popUpInvisibleAddEditButtonControl(evt);
            }
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {
            }
        });

        jLabel5.setText("ID:");

        javax.swing.GroupLayout table_panelLayout = new javax.swing.GroupLayout(table_panel);
        table_panel.setLayout(table_panelLayout);
        table_panelLayout.setHorizontalGroup(
            table_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(table_panelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(table_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(table_panelLayout.createSequentialGroup()
                        .addComponent(statusLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 1213, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(table_panelLayout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 848, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addGroup(table_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(table_panelLayout.createSequentialGroup()
                                .addComponent(deselectBtn)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(exitBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(table_panelLayout.createSequentialGroup()
                                .addGroup(table_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, table_panelLayout.createSequentialGroup()
                                        .addComponent(jLabel1)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(table_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, table_panelLayout.createSequentialGroup()
                                                .addComponent(addBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(editBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(deleteBtn)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(clearBtn))
                                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, table_panelLayout.createSequentialGroup()
                                                .addGroup(table_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                                    .addGroup(table_panelLayout.createSequentialGroup()
                                                        .addComponent(nameTF, javax.swing.GroupLayout.PREFERRED_SIZE, 176, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addGap(10, 10, 10)
                                                        .addComponent(jLabel2))
                                                    .addComponent(jLabel5))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(table_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                    .addComponent(roomNoCB, 0, 58, Short.MAX_VALUE)
                                                    .addComponent(idTF)))))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, table_panelLayout.createSequentialGroup()
                                        .addGroup(table_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addGroup(table_panelLayout.createSequentialGroup()
                                                .addComponent(jLabel3)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(checkInMCB, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(jLabel7))
                                            .addGroup(table_panelLayout.createSequentialGroup()
                                                .addGap(0, 0, Short.MAX_VALUE)
                                                .addComponent(jLabel4)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(checkOutMCB, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(jLabel8)))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(table_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(checkOutDCB, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(checkInDCB, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(table_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addGroup(table_panelLayout.createSequentialGroup()
                                                .addComponent(jLabel10)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(checkOutYCB, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                            .addGroup(table_panelLayout.createSequentialGroup()
                                                .addComponent(jLabel9)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(checkInYCB, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                        .addGap(34, 34, 34)))
                                .addContainerGap())))))
        );
        table_panelLayout.setVerticalGroup(
            table_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, table_panelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(table_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 382, Short.MAX_VALUE)
                    .addGroup(table_panelLayout.createSequentialGroup()
                        .addGroup(table_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(idTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel5))
                        .addGap(18, 18, 18)
                        .addGroup(table_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(nameTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel1)
                            .addComponent(roomNoCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(table_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(table_panelLayout.createSequentialGroup()
                                .addGroup(table_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel3)
                                    .addComponent(jLabel7)
                                    .addComponent(checkInMCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(checkInDCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(table_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel4)
                                    .addComponent(jLabel8)
                                    .addComponent(checkOutMCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(checkOutDCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(table_panelLayout.createSequentialGroup()
                                .addGroup(table_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel9)
                                    .addComponent(checkInYCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(table_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel10)
                                    .addComponent(checkOutYCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addGap(32, 32, 32)
                        .addGroup(table_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(clearBtn)
                            .addGroup(table_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(deleteBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(editBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(addBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(table_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, table_panelLayout.createSequentialGroup()
                                .addComponent(statusLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(29, 29, 29)
                                .addComponent(exitBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(deselectBtn, javax.swing.GroupLayout.Alignment.TRAILING))))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(table_panel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(table_panel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents
    
    private void exitBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitBtnActionPerformed
        System.exit(0);
    }//GEN-LAST:event_exitBtnActionPerformed

    private void addBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addBtnActionPerformed
        insertData(getForm());
        tableModel.setRowCount(0);
        displayReservationList();
        idTF.setText(null);
    }//GEN-LAST:event_addBtnActionPerformed

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
        displayReservationList();
        preloadComboBox();
        idTF.setEnabled(false);
        deleteBtn.setEnabled(false);
        editBtn.setEnabled(false);
        addBtn.setEnabled(false);
        schedTable.getColumnModel().getColumn(0).setPreferredWidth(10);
        schedTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        schedTable.getColumnModel().getColumn(2).setPreferredWidth(0);
        schedTable.getColumnModel().getColumn(3).setPreferredWidth(30);
        schedTable.getColumnModel().getColumn(4).setPreferredWidth(30);
        schedTable.getColumnModel().getColumn(5).setPreferredWidth(80);
    }//GEN-LAST:event_formWindowOpened

    private void deleteBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteBtnActionPerformed
      
        int decision = JOptionPane.showConfirmDialog(null, 
                                  "All selected rows will be deleted permanently, are you sure?", 
                                  "Confirm Delete", 
                                  JOptionPane.YES_NO_OPTION); 
        
        if (decision == JOptionPane.YES_OPTION) {
        
            int[] selectedRows = schedTable.getSelectedRows();

            for (int i=selectedRows.length-1 ; i >= 0 ; i--) {
                deleteData(Integer.parseInt(tableModel.getValueAt(selectedRows[i],0).toString()));
            }

            tableModel.setRowCount(0);
            displayReservationList();
            
            buttonControlAddEdit();
            buttonControlEditDelete();
            idTF.setText(null);
        }
        
    }//GEN-LAST:event_deleteBtnActionPerformed
    
    private void editBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editBtnActionPerformed
        
        int decision = JOptionPane.showConfirmDialog(null, 
                                  "The selected row will be overwritten, are you sure?", 
                                  "Confirm Edit", 
                                  JOptionPane.YES_NO_OPTION); 
        
        if (decision == JOptionPane.YES_OPTION) {
        
            updateData(getForm());
            tableModel.setRowCount(0);
            displayReservationList();
            idTF.setText(null);
        }

        
    }//GEN-LAST:event_editBtnActionPerformed

    private void schedTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_schedTableMouseClicked
        setForm(schedTable.getSelectedRow());
    }//GEN-LAST:event_schedTableMouseClicked

    private void mouseEditDeleteButtonControl(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_mouseEditDeleteButtonControl
        buttonControlEditDelete();
    }//GEN-LAST:event_mouseEditDeleteButtonControl

    private void keyboardEditDeleteButtonControl(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_keyboardEditDeleteButtonControl
        buttonControlEditDelete();
    }//GEN-LAST:event_keyboardEditDeleteButtonControl

    private void schedTableKeyboardReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_schedTableKeyboardReleased
        setForm(schedTable.getSelectedRow());
//        String rowData = currentTable.get(schedTable.getSelectedRow());
//        String[] data = rowData.split("\t");
//        
//        nameTF.setText(data[0]);
//        
//        roomNoCB.setSelectedItem(data[1]);
//        
//        String[] checkIn = data[2].split("-");
//        checkInMCB.setSelectedItem(checkIn[0]);
//        checkInDCB.setSelectedItem(checkIn[1]);
//        checkInYCB.setSelectedItem(checkIn[2]);
//
//        String[] checkOut = data[3].split("-");
//        checkOutMCB.setSelectedItem(checkOut[0]);
//        checkOutDCB.setSelectedItem(checkOut[1]);
//        checkOutYCB.setSelectedItem(checkOut[2]);
    }//GEN-LAST:event_schedTableKeyboardReleased

    private void clearBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearBtnActionPerformed
        clearForm();
    }//GEN-LAST:event_clearBtnActionPerformed

    private void keyboardAddEditButtonControl(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_keyboardAddEditButtonControl
        buttonControlAddEdit();
    }//GEN-LAST:event_keyboardAddEditButtonControl

    private void mouseAddEditButtonControl(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_mouseAddEditButtonControl
        buttonControlAddEdit();
    }//GEN-LAST:event_mouseAddEditButtonControl

    private void popUpInvisibleAddEditButtonControl(javax.swing.event.PopupMenuEvent evt) {//GEN-FIRST:event_popUpInvisibleAddEditButtonControl
        buttonControlAddEdit();
    }//GEN-LAST:event_popUpInvisibleAddEditButtonControl

    private void deselectBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deselectBtnActionPerformed
        schedTable.clearSelection();
        editBtn.setEnabled(false);
        deleteBtn.setEnabled(false);
        idTF.setText(null);
    }//GEN-LAST:event_deselectBtnActionPerformed

    private void checkInMCBPopupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {//GEN-FIRST:event_checkInMCBPopupMenuWillBecomeInvisible
        dateValidation("in");
    }//GEN-LAST:event_checkInMCBPopupMenuWillBecomeInvisible

    private void checkOutMCBPopupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {//GEN-FIRST:event_checkOutMCBPopupMenuWillBecomeInvisible
        dateValidation("out");
//        String[] thirtyOne = {"MM","1","3","5","7","8","10","12"};
//        String[] thirty = {"4","6","9","11"};
//        
//        Object temp = checkOutDCB.getSelectedItem();
//        
//        if (checkOutMCB.getSelectedItem().equals("2")) {
//            checkOutDCB.removeAllItems();
//            checkOutDCB.addItem("DD");
//            int feb = 29;
//            
//            if (checkOutYCB.getSelectedItem() != "YYYY") {
//                if ( ( (Integer.parseInt( (String) checkOutYCB.getSelectedItem())) % 4 ) != 0) {
//                    feb--;
//                }
//            }
//            for (int i = 1 ; i <= feb ; i++) {
//                    checkOutDCB.addItem(Integer.toString(i));
//            }
//        } else {
//            checkOutDCB.removeAllItems();
//            checkOutDCB.addItem("DD");
//            for (String s : thirtyOne) {
//                if (checkOutMCB.getSelectedItem().equals(s)){
//                    for (int i = 1 ; i <= 31 ; i++) {
//                        checkOutDCB.addItem(Integer.toString(i));
//                    }
//                    break;
//                }
//            }
//            for (String s : thirty) {
//                if (checkOutMCB.getSelectedItem().equals(s)) {
//                    for (int i = 1 ; i <= 30 ; i++) {
//                        checkOutDCB.addItem(Integer.toString(i));
//                    }
//                    break;
//                }
//            }
//        }
//        checkOutDCB.setSelectedItem(temp);
    }//GEN-LAST:event_checkOutMCBPopupMenuWillBecomeInvisible
    
    /**
     * @param args the command line arguments
     */
    
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MainFrame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addBtn;
    private javax.swing.JComboBox<String> checkInDCB;
    private javax.swing.JComboBox<String> checkInMCB;
    private javax.swing.JComboBox<String> checkInYCB;
    private javax.swing.JComboBox<String> checkOutDCB;
    private javax.swing.JComboBox<String> checkOutMCB;
    private javax.swing.JComboBox<String> checkOutYCB;
    private javax.swing.JButton clearBtn;
    private javax.swing.JButton deleteBtn;
    private javax.swing.JButton deselectBtn;
    private javax.swing.JButton editBtn;
    private javax.swing.JButton exitBtn;
    private javax.swing.JTextField idTF;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField nameTF;
    private javax.swing.JComboBox<String> roomNoCB;
    private javax.swing.JTable schedTable;
    private javax.swing.JLabel statusLabel;
    private javax.swing.JPanel table_panel;
    // End of variables declaration//GEN-END:variables
}
