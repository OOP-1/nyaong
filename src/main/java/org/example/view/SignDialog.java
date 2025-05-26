package org.example.view;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.Optional;

public class SignDialog {

    /**
     * 사용자에게 서명 여부 확인 다이얼로그를 띄움
     *
     * @return true: 서명 진행 / false: 취소
     */
    public boolean showConfirmation() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("전자서명 확인");
        alert.setHeaderText("해당 메세지에 서명하시겠습니까?");
        alert.setContentText("확인을 누르면 전자서명이 등록됩니다.");

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    /**
     * 이미 서명된 메시지임을 알리는 경고창
     */
    public void showAlreadySignedAlert() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("이미 서명됨");
        alert.setHeaderText("이미 이 메시지에 전자서명을 완료하였습니다.");
        alert.setContentText("더 이상 서명할 수 없습니다.");
        alert.showAndWait();
    }

    /**
     * 서명 완료 알림
     */
    public void showSuccessAlert() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("서명 완료");
        alert.setHeaderText(null);
        alert.setContentText("전자서명이 성공적으로 등록되었습니다.");
        alert.showAndWait();
    }

    /**
     * 서명 실패 알림
     */
    public void showFailureAlert() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("서명 실패");
        alert.setHeaderText("전자서명 등록에 실패하였습니다.");
        alert.setContentText("다시 시도해 주세요.");
        alert.showAndWait();
    }
}