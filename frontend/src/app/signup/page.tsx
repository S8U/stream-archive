"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { Loader2 } from "lucide-react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Button } from "@/components/ui/button";
import { useSignup, useLogin } from "@/lib/api/endpoints/auth/auth";
import { toast } from "sonner";

export default function SignupPage() {
    const router = useRouter();
    const [username, setUsername] = useState("");
    const [name, setName] = useState("");
    const [password, setPassword] = useState("");
    const [confirmPassword, setConfirmPassword] = useState("");

    const signupMutation = useSignup();
    const loginMutation = useLogin();

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();

        // 비밀번호 확인
        if (password !== confirmPassword) {
            toast.error("비밀번호가 일치하지 않습니다.", { position: "top-center" });
            return;
        }

        try {
            await signupMutation.mutateAsync({
                data: { username, name, email: "", password },
            });

            // 회원가입 성공 후 자동 로그인
            await loginMutation.mutateAsync({
                data: { username, password },
            });

            router.push("/");
        } catch (error) {
            toast.error("회원가입에 실패했습니다.", { position: "top-center" });
        }
    };

    return (
        <div className="min-h-screen flex items-center justify-center bg-background px-4">
            <Card className="w-full max-w-sm">
                <CardHeader className="text-center">
                    <CardTitle className="text-2xl font-bold">회원가입</CardTitle>
                </CardHeader>
                <form onSubmit={handleSubmit}>
                    <CardContent className="space-y-4">
                        <div className="space-y-2">
                            <Label htmlFor="username">아이디</Label>
                            <Input
                                id="username"
                                type="text"
                                value={username}
                                onChange={(e) => setUsername(e.target.value)}
                                required
                                disabled={signupMutation.isPending}
                            />
                        </div>
                        <div className="space-y-2">
                            <Label htmlFor="password">비밀번호</Label>
                            <Input
                                id="password"
                                type="password"
                                value={password}
                                onChange={(e) => setPassword(e.target.value)}
                                required
                                disabled={signupMutation.isPending}
                            />
                        </div>
                        <div className="space-y-2">
                            <Label htmlFor="confirmPassword">비밀번호 확인</Label>
                            <Input
                                id="confirmPassword"
                                type="password"
                                value={confirmPassword}
                                onChange={(e) => setConfirmPassword(e.target.value)}
                                required
                                disabled={signupMutation.isPending}
                            />
                        </div>
                        <div className="space-y-2">
                            <Label htmlFor="name">이름</Label>
                            <Input
                                id="name"
                                type="text"
                                value={name}
                                onChange={(e) => setName(e.target.value)}
                                required
                                disabled={signupMutation.isPending}
                            />
                        </div>

                        <Button
                            type="submit"
                            className="w-full"
                            disabled={signupMutation.isPending}
                        >
                            {signupMutation.isPending ? (
                                <>
                                    <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                                    가입 중...
                                </>
                            ) : (
                                "회원가입"
                            )}
                        </Button>
                        <p className="text-sm text-muted-foreground text-center">
                            이미 계정이 있으신가요?{" "}
                            <Link href="/login" className="text-primary hover:underline">
                                로그인
                            </Link>
                        </p>
                    </CardContent>
                </form>
            </Card>
        </div>
    );
}
