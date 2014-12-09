package com.wt.studio.plugin.json.internel.model.single;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.text.Position;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wt.studio.plugin.json.internel.core.util.reader.JsonReader;
import com.wt.studio.plugin.json.internel.core.util.reader.JsonReaderException;
import com.wt.studio.plugin.json.internel.model.JsonArray;
import com.wt.studio.plugin.json.internel.model.JsonModel;
import com.wt.studio.plugin.json.internel.model.JsonModelType;

public class JsonArrayBuilder implements JsonModelBuilder {

	private static final Logger LOG = LoggerFactory.getLogger(JsonArrayBuilder.class);

	private static final JsonModelBuilderFactory JSON_MODEL_BUILDER_FACTORY = new JsonModelBuilderFactory();

	public static final char OPEN_ARRAY = '[';
	public static final char CLOSE_ARRAY = ']';

	@Override
	public JsonModel buildModel(JsonReader parser) throws JsonReaderException {
		LOG.debug("JsonArrayBuilder");

		char ch;
		List<JsonModel> valueModels = new LinkedList<JsonModel>();
		boolean success = true;
		int openingOffset = parser.getPosition();
		do {
			ch = parser.getNextClean();

			JsonModelBuilder jsonModelBuilder = JSON_MODEL_BUILDER_FACTORY.getValueModelBuilder(ch);
			if (jsonModelBuilder  != null) {
				JsonModel valueModel = jsonModelBuilder.buildModel(parser);
				valueModels.add(valueModel);
			}

			if (jsonModelBuilder instanceof JsonErrorBuilder) {
				success = false;
				break;
			}

			ch = parser.getCurrent();
		} while (ch == ',');

		if (success && ch != CLOSE_ARRAY) {
			success = false;
			valueModels.add(new JsonModel(JsonModelType.Error, new Position(parser.getPosition(), 0), new Position(openingOffset, parser.getPosition() - openingOffset)));
		}

		if (success) {
			ch = parser.getNextClean();
		}

		return new JsonArray(valueModels, new Position(openingOffset, parser.getPosition() - openingOffset),
				new Position(openingOffset, parser.getPosition() - openingOffset));
	}
}
