package com.devoxx.genie.chatmodel;

import com.devoxx.genie.chatmodel.anthropic.AnthropicChatModelFactory;
import com.devoxx.genie.chatmodel.gpt4all.GPT4AllChatModelFactory;
import com.devoxx.genie.chatmodel.groq.GroqChatModelFactory;
import com.devoxx.genie.chatmodel.lmstudio.LMStudioChatModelFactory;
import com.devoxx.genie.chatmodel.mistral.MistralChatModelFactory;
import com.devoxx.genie.chatmodel.ollama.OllamaChatModelFactory;
import com.devoxx.genie.chatmodel.openai.OpenAIChatModelFactory;
import com.devoxx.genie.model.ChatModel;
import com.devoxx.genie.model.Constant;
import com.devoxx.genie.model.enumarations.ModelProvider;
import com.devoxx.genie.model.request.ChatMessageContext;
import com.devoxx.genie.ui.SettingsState;
import com.intellij.ide.util.PropertiesComponent;
import dev.langchain4j.model.chat.ChatLanguageModel;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import static com.devoxx.genie.ui.DevoxxGenieSettingsManager.MODEL_PROVIDER;

@Setter
public class ChatModelProvider {

    private final Map<ModelProvider, ChatModelFactory> factories = new HashMap<>();

    private String modelName;

    public ChatModelProvider() {
        factories.put(ModelProvider.Ollama, new OllamaChatModelFactory());
        factories.put(ModelProvider.LMStudio, new LMStudioChatModelFactory());
        factories.put(ModelProvider.GPT4All, new GPT4AllChatModelFactory());
        factories.put(ModelProvider.OpenAI, new OpenAIChatModelFactory());
        factories.put(ModelProvider.Mistral, new MistralChatModelFactory());
        factories.put(ModelProvider.Anthropic, new AnthropicChatModelFactory());
        factories.put(ModelProvider.Groq, new GroqChatModelFactory());
    }

    /**
     * Get the chat language model for selected model provider.
     * @param chatMessageContext the chat message context
     * @return the chat language model
     */
    public ChatLanguageModel getChatLanguageModel(@NotNull ChatMessageContext chatMessageContext) {
        ModelProvider provider = getLanguageModelProvider(chatMessageContext.getLlmProvider());
        ChatModelFactory factory = factories.get(provider);
        if (factory == null) {
            throw new IllegalArgumentException("No factory for provider: " + provider);
        }
        return factory.createChatModel(initChatModel(chatMessageContext));
    }

    /**
     * Get the model provider.
     * @param defaultValue the default value
     * @return the model provider
     */
    private ModelProvider getLanguageModelProvider(String defaultValue) {
        String value = PropertiesComponent.getInstance().getValue(MODEL_PROVIDER, defaultValue);
        return ModelProvider.valueOf(value);
    }

    /**
     * Initialize chat model settings by default or by user settings.
     * @return the chat model
     */
    public @NotNull ChatModel initChatModel(@NotNull ChatMessageContext chatMessageContext) {
        ChatModel chatModel = new ChatModel();
        SettingsState settingsState = SettingsState.getInstance();
        setMaxOutputTokens(settingsState, chatModel);

        chatModel.setTemperature(settingsState.getTemperature());
        chatModel.setMaxRetries(settingsState.getMaxRetries());
        chatModel.setTopP(settingsState.getTopP());
        chatModel.setTimeout(settingsState.getTimeout());
        chatModel.setModelName(chatMessageContext.getModelName());
        return chatModel;
    }

    /**
     * Set max output tokens.
     * @param settingsState the settings state
     * @param chatModel the chat model
     */
    private static void setMaxOutputTokens(@NotNull SettingsState settingsState, ChatModel chatModel) {
        Integer maxOutputTokens = settingsState.getMaxOutputTokens();
        if (maxOutputTokens == 0) {
            chatModel.setMaxTokens(Constant.MAX_OUTPUT_TOKENS);
        } else {
            chatModel.setMaxTokens(maxOutputTokens);
        }
    }
}
