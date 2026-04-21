package server.manager;

import common.model.HumanBeing;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Обёртка для коллекции HumanBeing, используемая для XML-сериализации.
 *
 * <p>Класс предназначен для корректной сериализации коллекции в XML-формат
 * с помощью библиотеки Jackson. Обеспечивает правильное форматирование
 * корневого элемента и элементов коллекции.</p>
 *
 * <p>Структура XML-файла:
 * <pre>
 * &lt;collection&gt;
 *     &lt;humanBeing&gt;...&lt;/humanBeing&gt;
 *     &lt;humanBeing&gt;...&lt;/humanBeing&gt;
 * &lt;/collection&gt;
 * </pre>
 * </p>
 *
 * @author Полина
 * @version 1.0
 * @since 2026-04-20
 * @see HumanBeing
 * @see FileManager
 */
@JacksonXmlRootElement(localName = "collection")
public class CollectionWrapper implements Serializable {

    /** Версия для сериализации. */
    private static final long serialVersionUID = 1L;

    /**
     * Коллекция объектов HumanBeing для сериализации.
     * Аннотации Jackson определяют формат XML-представления.
     */
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "humanBeing")
    private Deque<HumanBeing> collection = new ArrayDeque<>();

    /**
     * Конструктор по умолчанию для Jackson.
     */
    public CollectionWrapper() {
    }

    /**
     * Конструктор с инициализацией коллекции.
     *
     * @param collection коллекция для оборачивания
     */
    public CollectionWrapper(Deque<HumanBeing> collection) {
        this.collection = collection;
    }

    /**
     * Возвращает обёрнутую коллекцию.
     *
     * @return коллекция объектов HumanBeing
     */
    public Deque<HumanBeing> getCollection() {
        return collection;
    }

    /**
     * Устанавливает коллекцию для обёртки.
     *
     * @param collection новая коллекция объектов HumanBeing
     */
    public void setCollection(Deque<HumanBeing> collection) {
        this.collection = collection;
    }
}