import { cn } from "@/lib/utils";

/**
 * StreamArchive 로고
 *
 * 폴더(보관) 앞면이 재생 화면(방송)인 모양이다.
 * 뒤쪽 폴더 탭은 글자색을 옅게 칠해 앞면과 구분한다.
 * viewBox를 도형에 딱 맞게 잘라 여백이 없어서, 높이만 주면 너비가 정해진다.
 *
 * besideText는 글자 줄 옆에 둘 때 쓴다.
 * 이때 부모는 items-baseline으로 정렬해야 하고, 로고 크기는 글자 크기를 따른다.
 * 로고 높이는 대문자 높이의 1.6배이고, 로고 가운데를 대문자 가운데에 맞춘다.
 * 1.22배는 글자에 눌려 보였고, 제미나이 시안의 약 2배는 그림이 글자를 눌러서 그 사이로 골랐다.
 *
 * 파비콘(app/icon.svg)과 README 로고(docs/images/logo-*.svg)도 같은 도형이라, 도형을 바꾸면 함께 바꾼다.
 */
export function Logo({ className, besideText = false }: { className?: string; besideText?: boolean }) {
    return (
        <svg
            viewBox="3.5 6.4 25 19.8"
            fill="currentColor"
            aria-hidden="true"
            className={cn(
                "aspect-[25/19.8] w-auto shrink-0",
                // Pretendard 대문자 높이 0.707em(1448/2048)의 1.6배. 기준선 정렬은 로고 밑변을 기준선에 붙이고 margin을 무시하므로, 대문자 높이의 0.3배만큼 translate로 내린다
                besideText && "h-[1.131em] translate-y-[0.212em]",
                className,
            )}
        >
            <path
                fillOpacity={0.44}
                d="M3.5 8.6C3.5 7.38 4.48 6.4 5.7 6.4H13.1C13.85 6.4 14.55 6.76 14.98 7.37L17.44 10.8V12H3.5Z"
            />
            <path
                fillRule="evenodd"
                d="M6.7 10H25.3Q28.5 10 28.5 13.2V23Q28.5 26.2 25.3 26.2H6.7Q3.5 26.2 3.5 23V13.2Q3.5 10 6.7 10ZM13.64 14.36L19.58 17.64Q20.41 18.1 19.58 18.56L13.64 21.84Q12.81 22.3 12.81 21.35L12.81 14.85Q12.81 13.9 13.64 14.36Z"
            />
        </svg>
    );
}

/**
 * 로고와 서비스 이름을 한 줄로 붙인 묶음
 *
 * 메인 헤더와 관리자 사이드바가 같은 모양을 쓰도록 한곳에 둔다.
 * 이름은 본문 글자가 아니라 로고 글자로 보이게, 두 단어의 굵기와 진하기를 달리하고 글자 사이를 좁혀 한 덩어리로 읽히게 한다.
 * Archive는 회색을 따로 두지 않고 글자색을 옅게 칠한다.
 * 폴더 탭과 같은 방식이라 로고 전체가 글자색 한 가지로 칠해진다.
 */
export function LogoWithName({ className }: { className?: string }) {
    return (
        // 로고와 이름 사이는 로고 높이(1.131em)의 0.3배
        <span className={cn("inline-flex items-baseline gap-[0.34em] text-lg md:text-xl tracking-[-0.03em] text-primary whitespace-nowrap", className)}>
            <Logo besideText />
            <span>
                <span className="font-extrabold">Stream</span>
                <span className="font-medium opacity-[0.66]">Archive</span>
            </span>
        </span>
    );
}
