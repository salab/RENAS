import unittest

from renas.approaches.util.name import KgExpanderSplitter


class KgExpanderSplitterTest(unittest.TestCase):
    def setUp(self):
        self.__splitter = KgExpanderSplitter()

    def testCamel(self):
        result = self.__splitter.split("testCamelCase")
        self.assertEqual(["", "", "", ""], result["delimiter"])
        self.assertEqual(["LOWER", "TITLE", "TITLE"], result["case"])
        self.assertEqual(["test", "camel", "case"], result["split"])
        self.assertEqual(["LCAMEL"], result["pattern"])

    def testSnakeCase(self):
        result = self.__splitter.split("TEST_SNAKE_CASE")
        self.assertEqual(["", "_", "_", ""], result["delimiter"])
        self.assertEqual(["UPPER", "UPPER", "UPPER"], result["case"])
        self.assertEqual(["test", "snake", "case"], result["split"])
        self.assertEqual(["SNAKE"], result["pattern"])

    def testDelimiter(self):
        result = self.__splitter.split("$del_ImiteR__TESt_")
        self.assertEqual(["$", "_", "", "__", "", "_"], result["delimiter"])
        self.assertEqual(["LOWER", "TITLE", "UPPER", "UPPER", "TITLE"], result["case"])
        self.assertEqual(["del", "imite", "r", "te", "st"], result["split"])


if __name__ == "__main__":
    unittest.main()
