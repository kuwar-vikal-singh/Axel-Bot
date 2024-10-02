Add you own Api key 
in public interface ChatApiService {
    @POST("v1beta/models/gemini-1.5-flash-latest:generateContent?key= ADD_YOUR_KEY")
    Call<ChatResponse> sendMessage(@Body ChatRequest request);
}
