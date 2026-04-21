package server.manager;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import common.model.HumanBeing;

import java.io.*;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Менеджер для работы с файловым хранилищем коллекции.
 *
 * <p>Обеспечивает загрузку и сохранение коллекции объектов HumanBeing
 * в XML-формате. Использует библиотеку Jackson для сериализации
 * и десериализации XML.</p>
 *
 * <p>Поддерживает автоматическую обработку дат и времени через модуль
 * JavaTimeModule. XML-файл форматируется с отступами для удобочитаемости.</p>
 *
 * @author Полина
 * @version 1.0
 * @since 2026-04-20
 * @see HumanBeing
 * @see CollectionWrapper
 * @see CollectionManager
 */
public class FileManager {

    /** Имя файла для хранения коллекции. */
    private final String filename;

    /** Мapper для сериализации/десериализации XML. */
    private final XmlMapper xmlMapper;

    /**
     * Конструктор менеджера файлов.
     *
     * @param filename путь к файлу для сохранения/загрузки коллекции
     */
    public FileManager(String filename) {
        this.filename = filename;
        this.xmlMapper = XmlMapper.builder()
                .enable(SerializationFeature.INDENT_OUTPUT) // Включаем форматирование с отступами
                .build();
        xmlMapper.registerModule(new JavaTimeModule()); // Поддержка LocalDateTime
    }

    /**
     * Загружает коллекцию из XML-файла.
     *
     * <p>Если файл не существует, возвращает пустую коллекцию.
     * В случае ошибки чтения или десериализации пробрасывает исключение.</p>
     *
     * @return загруженная коллекция объектов HumanBeing (Deque)
     * @throws IOException если произошла ошибка при чтении файла или парсинге XML
     */
    public Deque<HumanBeing> loadCollection() throws IOException {
        File file = new File(filename);
        if (!file.exists()) {
            return new ArrayDeque<>();
        }

        try (FileInputStream fis = new FileInputStream(file);
             BufferedInputStream bis = new BufferedInputStream(fis)) {

            CollectionWrapper wrapper = xmlMapper.readValue(bis, CollectionWrapper.class);
            if (wrapper != null && wrapper.getCollection() != null) {
                return wrapper.getCollection();
            }
            return new ArrayDeque<>();
        }
    }

    /**
     * Сохраняет коллекцию в XML-файл.
     *
     * <p>Коллекция оборачивается в {@link CollectionWrapper} для правильной
     * структуры XML-документа. Файл сохраняется с отступами для удобства
     * чтения человеком.</p>
     *
     * @param collection коллекция объектов HumanBeing для сохранения
     * @throws IOException если произошла ошибка при записи в файл
     */
    public void saveCollection(Deque<HumanBeing> collection) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(filename);
             BufferedOutputStream bos = new BufferedOutputStream(fos)) {

            CollectionWrapper wrapper = new CollectionWrapper(collection);
            xmlMapper.writeValue(bos, wrapper);
        }
    }
}