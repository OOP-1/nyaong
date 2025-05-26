package org.example.view;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.example.control.LogController;
import org.example.model.Message;

import java.sql.Timestamp;
import java.util.List;

public class VerifyView {
    private final Stage stage;
    private final LogController logController = new LogController();
    private TableView<Message> messageTable;

    public VerifyView(Stage stage) {
        this.stage = stage;
    }

    public void show() {
        stage.setTitle("메시지 검증 로그 조회");

        VBox root = new VBox(10);
        root.setPadding(new Insets(10));

        // 검색 조건 UI
        HBox filterBox = new HBox(10);
        TextField chatRoomField = new TextField();
        chatRoomField.setPromptText("채팅방 ID");

        TextField senderField = new TextField();
        senderField.setPromptText("보낸 사람 ID");

        DatePicker fromDate = new DatePicker();
        fromDate.setPromptText("시작일");

        DatePicker toDate = new DatePicker();
        toDate.setPromptText("종료일");

        Button searchButton = new Button("검증 로그 조회");
        searchButton.setOnAction(e -> {
            Integer chatRoomId = chatRoomField.getText().isEmpty() ? null : Integer.parseInt(chatRoomField.getText());
            Integer senderId = senderField.getText().isEmpty() ? null : Integer.parseInt(senderField.getText());
            Timestamp from = fromDate.getValue() == null ? null : Timestamp.valueOf(fromDate.getValue().atStartOfDay());
            Timestamp to = toDate.getValue() == null ? null : Timestamp.valueOf(toDate.getValue().atTime(23, 59, 59));

            List<Message> result = logController.searchAndVerifyLogs(chatRoomId, senderId, from, to);
            updateTable(result);
        });

        filterBox.getChildren().addAll(chatRoomField, senderField, fromDate, toDate, searchButton);

        // 메시지 테이블 UI
        messageTable = new TableView<>();
        TableColumn<Message, Number> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(data -> data.getValue().messageIdProperty());

        TableColumn<Message, String> contentCol = new TableColumn<>("내용");
        contentCol.setCellValueFactory(data -> data.getValue().messageContentProperty());

        TableColumn<Message, Number> senderCol = new TableColumn<>("보낸 사람 ID");
        senderCol.setCellValueFactory(data -> data.getValue().senderIdProperty());

        TableColumn<Message, Timestamp> timeCol = new TableColumn<>("생성일");
        timeCol.setCellValueFactory(data -> data.getValue().createdAtProperty());

        TableColumn<Message, String> statusCol = new TableColumn<>("검증 상태");

        statusCol.setCellValueFactory(data -> {
            Message message = data.getValue();
            if (message.getBlockchainMessageId() == -1) {
                return new SimpleStringProperty("👀");
            } else {
                return new SimpleStringProperty(message.getVerificationStatus() ? "✅" : "❌");
            }
        });


        messageTable.getColumns().addAll(idCol, contentCol, senderCol, timeCol, statusCol);

        root.getChildren().addAll(filterBox, messageTable);

        Scene scene = new Scene(root, 800, 500);
        stage.setScene(scene);
        stage.show();
    }

    private void updateTable(List<Message> messages) {
        ObservableList<Message> observableList = FXCollections.observableArrayList(messages);
        messageTable.setItems(observableList);

        if (messages.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("조회 결과");
            alert.setHeaderText(null);
            alert.setContentText("조회된 로그가 없습니다.");
            alert.showAndWait();
        }
    }
}
