'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { useQueryClient } from '@tanstack/react-query';
import { Loader2 } from 'lucide-react';
import { toast } from 'sonner';
import { Button } from '@/components/ui/button';
import { Label } from '@/components/ui/label';
import { Switch } from '@/components/ui/switch';
import { useGetUserMeSettings, useUpdateUserMeSettings } from '@/lib/api/endpoints/user/user';

interface ProfileSettingsTabProps {
  onSaved: () => void;
}

interface SettingRowProps {
  id: string;
  label: string;
  description: string;
  checked: boolean;
  disabled: boolean;
  onCheckedChange: (checked: boolean) => void;
}

// 설정 한 줄. 설정이 늘면 줄을 더하고, 성격이 다른 설정이면 묶음(소제목)을 더한다.
function SettingRow({ id, label, description, checked, disabled, onCheckedChange }: SettingRowProps) {
  return (
    <div className="flex items-center justify-between gap-4">
      <div className="flex flex-col gap-1">
        <Label htmlFor={id}>{label}</Label>
        {/* 한국어 설명이 단어 중간에서 끊기지 않게 한다. */}
        <p className="text-sm text-muted-foreground break-keep">{description}</p>
      </div>
      <Switch id={id} checked={checked} disabled={disabled} onCheckedChange={onCheckedChange} />
    </div>
  );
}

// 내 정보의 설정 탭. 지금 있는 설정이 모두 관리자 전용이라 관리자에게만 보여준다.
export function ProfileSettingsTab({ onSaved }: ProfileSettingsTabProps) {
  const router = useRouter();
  const queryClient = useQueryClient();
  const { data: settings, isLoading } = useGetUserMeSettings();
  const updateSettingsMutation = useUpdateUserMeSettings();

  const [isShowUnlistedChannels, setIsShowUnlistedChannels] = useState(false);
  const [isShowUnlistedVideos, setIsShowUnlistedVideos] = useState(false);

  // 받아 온 설정으로 초기값 설정
  useEffect(() => {
    if (settings) {
      setIsShowUnlistedChannels(settings.isShowUnlistedChannels);
      setIsShowUnlistedVideos(settings.isShowUnlistedVideos);
    }
  }, [settings]);

  const handleSave = async () => {
    try {
      await updateSettingsMutation.mutateAsync({
        data: { isShowUnlistedChannels, isShowUnlistedVideos }
      });
      // 설정에 따라 목록에 나오는 채널과 동영상이 달라지므로 서버 화면과 받아 둔 데이터를 모두 다시 받는다.
      queryClient.invalidateQueries();
      router.refresh();
      toast.success('설정이 저장되었습니다.');
      onSaved();
    } catch {
      toast.error('설정 저장에 실패했습니다.');
    }
  };

  return (
    <>
      <div className="flex flex-col gap-3">
        <p className="text-xs font-medium text-muted-foreground">관리자 전용</p>
        <div className="flex flex-col gap-4">
          <SettingRow
            id="isShowUnlistedChannels"
            label="일부공개 채널 보기"
            description="사이드바와 홈·검색에 일부공개 채널과 그 채널의 동영상도 표시합니다."
            checked={isShowUnlistedChannels}
            disabled={isLoading}
            onCheckedChange={setIsShowUnlistedChannels}
          />
          <SettingRow
            id="isShowUnlistedVideos"
            label="일부공개 동영상 보기"
            description="홈·검색·채널 동영상 목록에 일부공개 동영상도 표시합니다."
            checked={isShowUnlistedVideos}
            disabled={isLoading}
            onCheckedChange={setIsShowUnlistedVideos}
          />
        </div>
      </div>
      <Button
        onClick={handleSave}
        disabled={isLoading || updateSettingsMutation.isPending}
        className="w-full"
      >
        {updateSettingsMutation.isPending && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
        저장
      </Button>
    </>
  );
}
