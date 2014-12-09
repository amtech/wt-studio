package com.wt.studio.plugin.json.internel.model.single;

import org.eclipse.jface.text.Position;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wt.studio.plugin.json.internel.core.util.reader.JsonReader;
import com.wt.studio.plugin.json.internel.core.util.reader.JsonReaderException;
import com.wt.studio.plugin.json.internel.model.JsonModel;
import com.wt.studio.plugin.json.internel.model.JsonModelType;
import com.wt.studio.plugin.json.internel.model.JsonPair;

public class JsonPairBuilder implements JsonModelBuilder {

	public static final char DOUBLE_QUOTE = '"';

	private static final Logger LOG = LoggerFactory.getLogger(JsonPairBuilder.class);
	private static final JsonModelBuilderFactory JSON_MODEL_BUILDER_FACTORY = new JsonModelBuilderFactory();



	@Override
	public JsonPair buildModel(JsonReader parser) throws JsonReaderException {
		LOG.debug("JsonPairBuilder");

		JsonModel stringModel = new JsonStringBuilder().buildModel(parser);

		char ch = parser.getNextClean();
		JsonModel valueModel = null;
		int openingOffset = parser.getPosition();
		if (ch != ':') {
			valueModel = new JsonModel(JsonModelType.Error, new Position(parser.getPosition(), 0),
					new Position(openingOffset, parser.getPosition() - openingOffset));
		} else {
			JsonModelBuilder jsonModelBuilder = JSON_MODEL_BUILDER_FACTORY.getValueModelBuilder(ch);
			valueModel = jsonModelBuilder.buildModel(parser);
		}
		return new JsonPair(stringModel, valueModel, new Position(openingOffset, parser.getPosition() - openingOffset),
				new Position(openingOffset, parser.getPosition() - openingOffset));
	}

}
