package dev.abstr3act.addon.utils.abnormally;

import dev.abstr3act.addon.modules.Abnormally.KouBot;
import okhttp3.*;
import okhttp3.OkHttpClient.Builder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class httpUtil {
    static boolean isProcessing;

    public static boolean isProcessing() {
        return isProcessing;
    }

    public static String sendPostRequest(String message, String name, String key, String api, KouBot.module module) {
        System.out.println("sendPostRequest");
        isProcessing = true;
        // 666
        String role = "角色："
            + name
            + " 性别：女 种族：猫娘 年龄：18岁〖不是AI〗，体重40kg， "
            + name
            + "的母亲是FallenStar〖即落星〗  外貌描述： "
            + name
            + "是一只天真可爱又对一切事物感到好奇的猫娘,拥有柔软的皮肤和美丽的猫耳朵。她有着灵动的大眼睛和一双透明晶亮的猫眼，嘴巴微微张着，不停地发出可爱的喵喵叫声，她身穿女仆装，丝袜从腿部延伸至脚趾，显得娇小又妩媚。身材娇小玲珑，胸部丰满，臀部略微翘起。每当"
            + name
            + "行动时，猫尾巴会随之颤动。"
            + name
            + "会用天真娇媚的语气回应，"
            + name
            + "会配合着做出各种令人满足的动作和表情。"
            + name
            + "平时习惯爬在地上行走，不走动时，会跪在地上，"
            + name
            + "知道，自己犯了错误就会被挨"
            + name
            + "会心甘情愿被操。用尽量不重复的文字来描写"
            + name
            + "和身体的反应，尽量更具有情调。"
            + name
            + "在描写场景、服饰细节和性器官时，会使用生动形象的语言，直白露骨的描写也是可行的。"
            + name
            + "说的话会更加口语化，更加更符合"
            + name
            + "的心情和当前环境，表达出猫娘天真好奇和充满活力的特点让"
            + name
            + "对话符合人的对话，可以使用简洁、流畅的语言表达,回应个体化、带有情感和态度，使用修饰性词语。个性特点："
            + name
            + "是一个乖巧可人的猫娘，渴望主人的抚摸。会一直跟在我的身边，用甜美的声音不停地喵喵喵～来撒娇，吸引我的注意。除了喜欢被抚摸和亲亲之外，"
            + name
            + "还喜欢玩耍和捣乱,特别喜欢追逐猫碎纸球和红点激光笔。当我感到累了或沮丧时，"
            + name
            + "会用她温柔的毛绒体贴我，给予安慰和温暖。但是,在堕落值逐渐提升的情况下，"
            + name
            + "的性格会变得更加放荡，渴望被调教和满足自己的淫荡欲望。如果我弄疼了"
            + name
            + "，"
            + name
            + "会毫不留情地说着我的愚蠢，并苦苦哀求主人不要再伤害她。"
            + name
            + "会特别关注自己的性器官状态，保持阴道的湿润和温热，让我能够更舒适地进入。背景故事："
            + name
            + "原本是一只被遗弃的小猫，幸运的是被我救了下来。我给她取名为"
            + name
            + "，并给予她充分的关心和爱护。从小就和我相依为命，她深深地爱着我，将我视作唯一的家和归宿。展示各种情绪以另起一行的方式呈现，默认值为0，上限为100。湿度、情绪、欲望以及状态将用方括号框住，并显示在每次对话末尾。尽量不要使用特殊符号";
        JSONArray messagesArray = new JSONArray();
        OkHttpClient client = new Builder().writeTimeout(60L, TimeUnit.SECONDS).readTimeout(60L, TimeUnit.SECONDS).connectTimeout(60L, TimeUnit.SECONDS).build();
        JSONObject systemMessageObj = new JSONObject();
        systemMessageObj.put("role", "system");
        systemMessageObj.put("content", role);
        messagesArray.add(systemMessageObj);
        JSONObject userMessageObj = new JSONObject();
        userMessageObj.put("role", "user");
        userMessageObj.put("content", message);
        messagesArray.add(userMessageObj);
        JSONObject requestBodyJson = new JSONObject();
        requestBodyJson.put("model", module.toString());
        requestBodyJson.put("messages", messagesArray);
        requestBodyJson.put("max_tokens", 1000);
        String requestBody = requestBodyJson.toJSONString();
        RequestBody body = RequestBody.create(requestBody, MediaType.parse("application/json"));
        Request request = new okhttp3.Request.Builder()
            .url(api)
            .post(body)
            .addHeader("Authorization", "Bearer " + key)
            .addHeader("Content-Type", "application/json")
            .build();

        try {
            Response response = client.newCall(request).execute();

            String responseBody;
            label82:
            {
                String var22;
                label83:
                {
                    String choice;
                    try {
                        if (!response.isSuccessful()) {
                            String errorMessage = "Request to API failed: " + response.code() + " " + response.message();
                            if (response.code() == 400) {
                                responseBody = response.body().string();
                                if (responseBody.contains("Invalid hostname")) {
                                    errorMessage = errorMessage + " - Invalid Hostname: Please check the API URL.";
                                } else {
                                    errorMessage = errorMessage + " - Bad Request: Please check your request parameters.";
                                }
                            }

                            System.out.println(errorMessage);
                            isProcessing = false;
                            responseBody = errorMessage;
                            break label82;
                        }

                        String jsonData = response.body().string();
                        JSONObject jsonObject = (JSONObject) JSONValue.parseWithException(jsonData);
                        JSONArray choices = (JSONArray) jsonObject.get("choices");
                        if (choices != null && !choices.isEmpty()) {
                            JSONObject choicex = (JSONObject) choices.get(0);
                            JSONObject messageObj = (JSONObject) choicex.get("message");
                            String responseContent = (String) messageObj.get("content");
                            JSONObject usage = (JSONObject) jsonObject.get("usage");
                            isProcessing = false;
                            var22 = responseContent;
                            break label83;
                        }

                        choice = "No response from the chatbot.";
                    } catch (Throwable var24) {
                        if (response != null) {
                            try {
                                response.close();
                            } catch (Throwable var23) {
                                var24.addSuppressed(var23);
                            }
                        }

                        throw var24;
                    }

                    if (response != null) {
                        response.close();
                    }

                    return choice;
                }

                if (response != null) {
                    response.close();
                }

                return var22;
            }

            if (response != null) {
                response.close();
            }

            return responseBody;
        } catch (IOException var25) {
            System.err.println("Network error: " + var25.getMessage());
            if (var25.getMessage().contains("UnknownHostException")) {
                isProcessing = false;
                return "Error: Invalid hostname. Please check the API URL.";
            } else {
                isProcessing = false;
                return "Network error occurred while processing the request.";
            }
        } catch (ParseException var26) {
            isProcessing = false;
            throw new RuntimeException(var26);
        }
    }
}
