package com.example.ai.mapper;

import com.example.ai.domain.AiConversation;
import com.example.ai.domain.AiMessage;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface AiChatMapper {
    int insertConversation(AiConversation conv);
    int updateConversationTitle(Long id, String title, Long userId);
    int deleteConversation(Long id, Long userId);
    List<AiConversation> selectConversationsByUserId(Long userId);
    AiConversation selectConversationById(Long id, Long userId);
    int insertMessage(AiMessage msg);
    List<AiMessage> selectMessagesByConversationId(Long conversationId);
    int deleteMessagesByConversationId(Long conversationId);
}
