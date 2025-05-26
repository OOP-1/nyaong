package org.example.view;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.model.Message;
import org.example.service.BlockchainMessageService;

import java.util.List;

public class SignView {
    private final BlockchainMessageService blockchainService = new BlockchainMessageService();

    public void showSigners(Message message) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("전자서명자 목록");

        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(20));

        List<String> signers = blockchainService.getMessageSigners(message);

        if (signers.isEmpty()) {
            vbox.getChildren().add(new Label("아직 전자서명한 사용자가 없습니다."));
        } else {
            vbox.getChildren().add(new Label("전자서명자 목록:"));
            for (String signer : signers) {
                vbox.getChildren().add(new Label("• " + signer));
            }
        }

        Button closeButton = new Button("닫기");
        closeButton.setOnAction(e -> stage.close());

        vbox.getChildren().add(closeButton);
        Scene scene = new Scene(vbox);
        stage.setScene(scene);
        stage.showAndWait();
    }
}
