export default function VideoPage({
    params,
}: {
    params: { uuid: string };
}) {
    const { uuid } = params;

    return (
        <div>
            <h1>비디오 페이지</h1>
            <p>비디오 UUID: {uuid}</p>
        </div>
    );
}