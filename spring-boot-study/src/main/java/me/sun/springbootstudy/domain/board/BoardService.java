package me.sun.springbootstudy.domain.board;

import lombok.RequiredArgsConstructor;
import me.sun.springbootstudy.domain.board.repository.BoardRepository;
import me.sun.springbootstudy.domain.member.Member;
import me.sun.springbootstudy.domain.member.MemberRepository;
import me.sun.springbootstudy.web.dto.board.BoardListResponseDto;
import me.sun.springbootstudy.web.dto.board.BoardSaveRequestDto;
import me.sun.springbootstudy.web.dto.board.BoardUpdateRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class BoardService {

    private final BoardRepository boardRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public Long save(BoardSaveRequestDto dto) {
        Member member = memberRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));

        return boardRepository.save(dto.toEntity(member)).getId();
    }

    public Page<BoardListResponseDto> findBoards(Pageable pageable) {
        return boardRepository.findAll(pageable).map(BoardListResponseDto::new);
    }

    @Transactional
    public Long update(Long id, BoardUpdateRequestDto dto) {
        Board findBoard = boardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시판이 존재하지 않습니다."));

        findBoard.update(dto.getTitle(), dto.getContent(), dto.getBoardType());

        return id;
    }

    @Transactional
    public void countBoardViews(Long id) {
        boardRepository.countViewsCount(id);
    }

    public Page<BoardListResponseDto> findBoardsByTitle(String title, Pageable pageable) {
        return boardRepository.findBoardsTitleWithPageable(title, pageable).map(BoardListResponseDto::new);
    }

    public boolean validateBoardMember(Long boardId, String email) {
        Board board = boardRepository.findWithMemberBy(boardId)
                .orElseThrow(() -> new IllegalArgumentException("게시판이 존재하지 않습니다."));
        return board.getMember().getEmail().equals(email);
    }

    @Transactional
    public void deleteBoard(Long id, String email) {
        Board board = boardRepository.findWithMemberBy(id)
                .orElseThrow(() -> new IllegalArgumentException("게시판이 존재하지 않습니다."));

        if (!board.getMember().getEmail().equals(email)) {
            throw new IllegalArgumentException("게시글 작성자만 삭제가 가능합니다.");
        }

        boardRepository.delete(board);
    }
}
