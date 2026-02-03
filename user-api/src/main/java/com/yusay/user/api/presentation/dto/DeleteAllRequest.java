package com.yusay.user.api.presentation.dto;

/**
 * 全件削除リクエスト
 * 確認メカニズムとして、明示的な確認文字列を要求する
 * 
 * @param confirmationText 確認文字列（"DELETE_ALL_USERS"である必要がある）
 */
public record DeleteAllRequest(
    String confirmationText
) {
    public static final String REQUIRED_CONFIRMATION = "DELETE_ALL_USERS";
    
    public boolean isConfirmed() {
        return REQUIRED_CONFIRMATION.equals(confirmationText);
    }
}
