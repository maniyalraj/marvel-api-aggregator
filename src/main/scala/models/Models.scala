package models

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder, HCursor}

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

case class Character(id: Int, name: String, description: String, thumbnail: Thumbnail)

object Character {
  implicit val characterEncoder: Encoder.AsObject[Character] = deriveEncoder[Character]
  implicit val characterDecoder: Decoder[Character] = deriveDecoder[Character]
  implicit val thumbnailEncoder: Encoder.AsObject[Thumbnail] = deriveEncoder[Thumbnail]
  implicit val thumbnailDecoder: Decoder[Thumbnail] = deriveDecoder[Thumbnail]
}

case class DateInfo(`type`: String, date: String)

case class Comic(title: String, issueNumber: String, releaseYear: Option[LocalDateTime])

object Comic {
  implicit val dateInfoDecoder: Decoder[DateInfo] = deriveDecoder[DateInfo]

  implicit val comicDecoder: Decoder[Comic] = (c: HCursor) => {
    for {
      title <- c.downField("title").as[String]
      issueNumber <- c
        .downField("issueNumber")
        .as[String]
        .orElse(
          c.downField("issueNumber")
            .as[Double]
            .map(_.toString)
            .orElse(
              c.downField("issueNumber").as[Int].map(_.toString)
            )
        )
      dates <- c.downField("dates").as[List[DateInfo]]
    } yield {
      val releaseDate = dates
        .find(_.`type` == "onsaleDate")
        .flatMap(dateInfo => parseDate(dateInfo.date))

      Comic(title, issueNumber, releaseDate)
    }
  }

  implicit val comicEncoder: Encoder.AsObject[Comic] = deriveEncoder[Comic]

  // Helper function to parse date strings into LocalDateTime
  private def parseDate(dateStr: String): Option[LocalDateTime] = {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ")
    try {
      Some(LocalDateTime.parse(dateStr, formatter))
    } catch {
      case _: Exception => None
    }
  }
}

case class Thumbnail(path: String, extension: String)
