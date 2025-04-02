package com.example.chatserver.chat.service;

import com.example.chatserver.chat.domain.ChatMessage;
import com.example.chatserver.chat.domain.ChatParticipant;
import com.example.chatserver.chat.domain.ChatRoom;
import com.example.chatserver.chat.domain.ReadStatus;
import com.example.chatserver.chat.dto.ChatMessageDto;
import com.example.chatserver.chat.dto.ChatRoomListResDto;
import com.example.chatserver.chat.repository.ChatMessageRepository;
import com.example.chatserver.chat.repository.ChatParticipantRepository;
import com.example.chatserver.chat.repository.ChatRoomRepository;
import com.example.chatserver.chat.repository.ReadStatusRepository;
import com.example.chatserver.member.domain.Member;
import com.example.chatserver.member.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class ChatService {
    private final ChatMessageRepository chatMessageRepository;
    private final ReadStatusRepository readStatusRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final MemberRepository memberRepository;

    // 메세지 저장
    public void saveMessage(Long roomId, ChatMessageDto dto) {
        /*
                작업 내용
                1. 채팅방 조회
                2. 보낸 사람에 대한 객체 조회 -> dto에 senderEmail이 들어있음
                3. 메세지 저장 -> 채팅창 객체, 보낸 사람 객체가 필요함
                4. 읽음 여부 저장 ->
         */
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("채팅방이 존재하지 않습니다."));

        Member sender = memberRepository.findByEmail(dto.getSenderEmail())
                .orElseThrow(() -> new EntityNotFoundException("해당 유저가 존재하지 않습니다."));

        ChatMessage chatMessage = ChatMessage.builder()
                .chatRoom(chatRoom)
                .member(sender)
                .content(dto.getMessage())
                .build();

        chatMessageRepository.save(chatMessage);

        // 읽음 여부 저장 (사용자 별로)

        // 1. 채팅방에 참여한 사람들 조회
        List<ChatParticipant> chatParticipants = chatParticipantRepository.findByChatRoom(chatRoom);
        for (ChatParticipant c : chatParticipants) {
            // 2. 읽음 여부 저장
            ReadStatus readStatus = ReadStatus.builder()
                    .chatRoom(chatRoom)
                    .member(c.getMember())
                    .chatMessage(chatMessage)
                    // c가 보낸 사람이면 읽음 상태를 true로
                    .isRead(c.getMember().equals(sender))
                    .build();
            readStatusRepository.save(readStatus);
        }
    }
    // 채팅방 생성
    public void createGroupRoom(String charRoomName){
        // 방을 만드는 사람은 필요 없다 ㅇㅇ -> 채팅방을 만드는 사람은 무조건 참여자가 되어야 함
        // 만든 사람의 이메일 정보
        String createByEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        // 만든 사람의 객체
        Member createBy = memberRepository.findByEmail(createByEmail)
                .orElseThrow(()->new EntityNotFoundException("해당 유저가 존재하지 않습니다."));

        // 채팅방 생성
        ChatRoom chatRoom = ChatRoom.builder()
                .name(charRoomName)
                .isGroupChat("Y")
                .build();
        // -> 채팅방 저장
        chatRoomRepository.save(chatRoom);

        // 채팅 참여자로 개설자를 추가
        ChatParticipant chatParticipant = ChatParticipant.builder()
                .chatRoom(chatRoom)
                .member(createBy)
                .build();
        chatParticipantRepository.save(chatParticipant);
    }
    // 채팅방 리스트 조회
    public List<ChatRoomListResDto> getGroupChatRooms(){
        List<ChatRoom> groupChatRooms = chatRoomRepository.findByIsGroupChat("Y");
        List<ChatRoomListResDto> dtos = new ArrayList<>();
        for (ChatRoom groupChatRoom : groupChatRooms) {
            dtos.add(ChatRoomListResDto.builder()
                            .roomId(groupChatRoom.getId())
                            .roomName(groupChatRoom.getName())
                            .build());
        }
        return dtos;
    }

    // 그룹 채팅방 참여 +) 참여자 정보는 Authentication에서 가져옴
    public void addParticipantToGroupRoom(Long roomId){
        // 채팅방 객체를 roomId를 통해서 가져와야 해
        // 그리고 ChatParticipant 객체를 생성해야 해

        // 1. 채팅방 조회
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("채팅방이 존재하지 않습니다."));

        // 2. 참여자 조회 -> Authentication에서 가져옴
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("해당 유저가 존재하지 않습니다."));

        // 3. 참여자 중복 체크 (검증)
        Optional<ChatParticipant> participant = chatParticipantRepository.findByChatRoomAndMember(chatRoom, member);

        // -> 참여자가 없으면 참여자 추가
        if (!participant.isPresent()) {
            addParticipantToRoom(chatRoom, member);
        }



    }
    private void addParticipantToRoom(ChatRoom chatRoom,Member member){
        // 4. 참여자 객체 생성
        ChatParticipant participant = ChatParticipant.builder()
                .chatRoom(chatRoom)
                .member(member)
                .build();

        // 5. 참여자 객체 저장
        chatParticipantRepository.save(participant);
    }
    public List<ChatMessageDto> getChatHistory(Long roomId){
        // 내가 해당 채팅방의 참여자가 아닌 경우 -> 에러 발생

        // 1. 채팅방 조회
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("채팅방이 존재하지 않습니다."));

        // 2. 참여자 조회 -> Authentication에서 가져옴
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("해당 유저가 존재하지 않습니다."));

        List<ChatParticipant> chatParticipants = chatParticipantRepository.findByChatRoom(chatRoom);
        // 3. 참여자 체크 (검증)
        boolean isParticipant = false;
        for (ChatParticipant cp : chatParticipants) {
            if (cp.getMember().equals(member)){
                isParticipant = true;
                break;
            }
        }
        if (!isParticipant) {
            throw new IllegalArgumentException("해당 채팅방에 참여하지 않은 유저입니다.");
        }

        List<ChatMessage> chatMessages = chatMessageRepository.findByChatRoomOrderByCreatedTimeAsc(chatRoom);

        List<ChatMessageDto> dtos = new ArrayList<>();

        for (ChatMessage chatMessage : chatMessages) {
            dtos.add(ChatMessageDto.builder()
                    .message(chatMessage.getContent())
                    .senderEmail(chatMessage.getMember().getEmail())
                    .build());
        }
        return dtos;
    }

    public boolean isRoomParticipant(Long roomId, String email) {
        // 1. 채팅방 조회
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("채팅방이 존재하지 않습니다."));

        // 2. 참여자 조회 -> Authentication에서 가져옴
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("해당 유저가 존재하지 않습니다."));
        // 3. 참여자 체크
        List<ChatParticipant> chatParticipants = chatParticipantRepository.findByChatRoom(chatRoom);

        for (ChatParticipant cp : chatParticipants) {
            if (cp.getMember().equals(member)){
                return true;
            }
        }

        return false;
    }
}
