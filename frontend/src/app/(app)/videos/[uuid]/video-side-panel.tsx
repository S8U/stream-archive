"use client";

import { Button } from "@/components/ui/button";
import { Separator } from "@/components/ui/separator";
import { X } from "lucide-react";
import type { ReactNode } from "react";

interface VideoSidePanelProps {
  /** 패널 제목. 헤더 가운데에 표시한다. */
  title: string;
  /** 닫기(X) 버튼을 누르면 호출한다. */
  onClose: () => void;
  children: ReactNode;
}

/**
 * 우측 채팅 영역을 덮는 패널의 공통 껍데기.
 *
 * 헤더(제목 + 닫기 버튼)와 레이아웃만 제공하고, 내용은 children으로 받는다.
 * 분석·요약 등 기능이 늘어도 같은 창 모양을 재사용한다. 헤더 크기는 채팅 헤더와 동일하게 맞춘다.
 */
export function VideoSidePanel({
  title,
  onClose,
  children,
}: VideoSidePanelProps) {
  return (
    <div className="bg-background flex h-full flex-col">
      <div className="grid grid-cols-[1fr_auto_1fr] items-center px-4 py-2">
        <h2 className="col-start-2 font-semibold">{title}</h2>
        <Button
          type="button"
          variant="ghost"
          size="icon"
          onClick={onClose}
          aria-label={`${title} 닫기`}
          className="col-start-3 justify-self-end -mr-2 h-7 w-7"
        >
          <X className="h-4 w-4" />
        </Button>
      </div>

      <Separator />

      <div className="min-h-0 flex-1">{children}</div>
    </div>
  );
}
