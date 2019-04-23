/* SplinesDefs.java
Copyright (C) 2012 Red Hat, Inc.

This file is part of IcedTea.

IcedTea is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2, or (at your option)
any later version.

IcedTea is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with IcedTea; see the file COPYING.  If not, write to the
Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
02110-1301 USA.

Linking this library statically or dynamically with other modules is
making a combined work based on this library.  Thus, the terms and
conditions of the GNU General Public License cover the whole
combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent
modules, and to copy and distribute the resulting executable under
terms of your choice, provided that you also meet, for each linked
independent module, the terms and conditions of the license of that
module.  An independent module is a module which is not derived from
or based on this library.  If you modify this library, you may extend
this exception to your version of the library, but you are not
obligated to do so.  If you do not wish to do so, delete this
exception statement from your version. */
package net.adoptopenjdk.icedteaweb.client.parts.splashscreen.impls.defaultsplashscreen2012;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class SplinesDefs {

    private final static Point[] blackLeaf = {
        new Point(267, 204),
        new Point(267, 205),
        new Point(267, 206),
        new Point(268, 206),
        new Point(268, 207),
        new Point(268, 208),
        new Point(268, 209),
        new Point(269, 209),
        new Point(269, 210),
        new Point(270, 210),
        new Point(270, 211),
        new Point(271, 211),
        new Point(271, 212),
        new Point(272, 212),
        new Point(273, 212),
        new Point(274, 212),
        new Point(275, 212),
        new Point(276, 212),
        new Point(277, 212),
        new Point(278, 212),
        new Point(279, 212),
        new Point(280, 212),
        new Point(281, 212),
        new Point(282, 212),
        new Point(283, 212),
        new Point(284, 212),
        new Point(285, 212),
        new Point(286, 212),
        new Point(287, 212),
        new Point(288, 212),
        new Point(289, 212),
        new Point(290, 212),
        new Point(291, 212),
        new Point(292, 212),
        new Point(293, 212),
        new Point(294, 212),
        new Point(295, 212),
        new Point(296, 212),
        new Point(297, 212),
        new Point(298, 212),
        new Point(299, 212),
        new Point(300, 212),
        new Point(301, 212),
        new Point(302, 212),
        new Point(303, 212),
        new Point(304, 212),
        new Point(305, 212),
        new Point(306, 212),
        new Point(307, 212),
        new Point(308, 212),
        new Point(309, 212),
        new Point(310, 212),
        new Point(311, 212),
        new Point(311, 213),
        new Point(312, 213),
        new Point(313, 213),
        new Point(314, 213),
        new Point(314, 214),
        new Point(315, 214),
        new Point(316, 214),
        new Point(317, 214),
        new Point(317, 215),
        new Point(318, 215),
        new Point(319, 215),
        new Point(319, 216),
        new Point(320, 216),
        new Point(321, 216),
        new Point(321, 217),
        new Point(322, 217),
        new Point(322, 218),
        new Point(323, 218),
        new Point(324, 218),
        new Point(325, 218),
        new Point(326, 218),
        new Point(327, 218),
        new Point(328, 218),
        new Point(329, 217),
        new Point(330, 216),
        new Point(331, 215),
        new Point(332, 214),
        new Point(333, 213),
        new Point(334, 212),
        new Point(334, 211),
        new Point(335, 210),
        new Point(335, 209),
        new Point(336, 208),
        new Point(337, 207),
        new Point(338, 206),
        new Point(338, 205),
        new Point(339, 204),
        new Point(340, 203),
        new Point(341, 202),
        new Point(342, 201),
        new Point(343, 200),
        new Point(344, 199),
        new Point(345, 198),
        new Point(346, 197),
        new Point(347, 196),
        new Point(348, 195),
        new Point(349, 194),
        new Point(350, 193),
        new Point(351, 192),
        new Point(352, 191),
        new Point(353, 190),
        new Point(354, 189),
        new Point(355, 188),
        new Point(356, 187),
        new Point(357, 186),
        new Point(358, 185),
        new Point(359, 184),
        new Point(360, 183),
        new Point(361, 182),
        new Point(362, 181),
        new Point(363, 180),
        new Point(364, 179),
        new Point(365, 179),
        new Point(366, 178),
        new Point(367, 177),
        new Point(368, 177),
        new Point(369, 176),
        new Point(370, 175),
        new Point(371, 175),
        new Point(372, 175),
        new Point(373, 175),
        new Point(373, 176),
        new Point(372, 177),
        new Point(372, 178),
        new Point(371, 179),
        new Point(371, 180),
        new Point(370, 181),
        new Point(369, 182),
        new Point(369, 183),
        new Point(368, 184),
        new Point(367, 185),
        new Point(367, 186),
        new Point(366, 187),
        new Point(365, 188),
        new Point(365, 189),
        new Point(364, 190),
        new Point(363, 191),
        new Point(363, 192),
        new Point(362, 193),
        new Point(361, 194),
        new Point(360, 195),
        new Point(360, 196),
        new Point(359, 197),
        new Point(359, 198),
        new Point(359, 199),
        new Point(358, 200),
        new Point(357, 201),
        new Point(356, 202),
        new Point(355, 203),
        new Point(355, 204),
        new Point(355, 205),
        new Point(354, 206),
        new Point(353, 207),
        new Point(353, 208),
        new Point(352, 209),
        new Point(351, 210),
        new Point(350, 211),
        new Point(350, 212),
        new Point(349, 213),
        new Point(349, 214),
        new Point(349, 215),
        new Point(348, 216),
        new Point(347, 217),
        new Point(346, 218),
        new Point(346, 219),
        new Point(345, 220),
        new Point(345, 221),
        new Point(345, 222),
        new Point(344, 223),
        new Point(344, 224),
        new Point(343, 225),
        new Point(342, 226),
        new Point(341, 227),
        new Point(341, 228),
        new Point(341, 229),
        new Point(340, 230),
        new Point(340, 231),
        new Point(340, 232),
        new Point(340, 233),
        new Point(340, 234),
        new Point(340, 235),
        new Point(341, 235),
        new Point(341, 236),
        new Point(342, 236),
        new Point(342, 237),
        new Point(343, 237),
        new Point(343, 238),
        new Point(343, 239),
        new Point(344, 239),
        new Point(344, 240),
        new Point(345, 240),
        new Point(345, 241),
        new Point(345, 242),
        new Point(346, 242),
        new Point(346, 243),
        new Point(347, 243),
        new Point(347, 244),
        new Point(348, 244),
        new Point(348, 245),
        new Point(348, 246),
        new Point(349, 246),
        new Point(349, 247),
        new Point(349, 248),
        new Point(349, 249),
        new Point(350, 249),
        new Point(350, 250),
        new Point(350, 251),
        new Point(351, 251),
        new Point(351, 252),
        new Point(351, 253),
        new Point(352, 253),
        new Point(352, 254),
        new Point(352, 255),
        new Point(353, 255),
        new Point(353, 256),
        new Point(353, 257),
        new Point(353, 258),
        new Point(354, 258),
        new Point(354, 259),
        new Point(354, 260),
        new Point(354, 261),
        new Point(354, 262),
        new Point(354, 263),
        new Point(355, 263),
        new Point(355, 264),
        new Point(355, 265),
        new Point(355, 266),
        new Point(355, 267),
        new Point(356, 267),
        new Point(356, 268),
        new Point(356, 269),
        new Point(356, 270),
        new Point(356, 271),
        new Point(356, 272),
        new Point(356, 273),
        new Point(357, 273),
        new Point(357, 274),
        new Point(357, 275),
        new Point(357, 276),
        new Point(357, 277),
        new Point(358, 277),
        new Point(358, 278),
        new Point(358, 279),
        new Point(358, 280),
        new Point(358, 281),
        new Point(358, 282),
        new Point(358, 283),
        new Point(358, 284),
        new Point(358, 285),
        new Point(358, 286),
        new Point(358, 287),
        new Point(358, 288),
        new Point(358, 289),
        new Point(358, 290),
        new Point(358, 291),
        new Point(358, 292),
        new Point(358, 293),
        new Point(358, 294),
        new Point(357, 295),
        new Point(357, 296),
        new Point(357, 297),
        new Point(357, 298),
        new Point(356, 299),
        new Point(356, 300),
        new Point(356, 301),
        new Point(356, 302),
        new Point(356, 303),
        new Point(356, 304),
        new Point(356, 305),
        new Point(357, 305),
        new Point(357, 306),
        new Point(358, 306),
        new Point(358, 307),
        new Point(359, 307),
        new Point(359, 308),
        new Point(360, 308),
        new Point(361, 308),
        new Point(361, 309),
        new Point(362, 309),
        new Point(363, 309),
        new Point(364, 309),
        new Point(365, 308),
        new Point(366, 308),
        new Point(367, 308),
        new Point(368, 307),
        new Point(369, 306),
        new Point(370, 306),
        new Point(371, 305),
        new Point(372, 305),
        new Point(373, 305),
        new Point(374, 304),
        new Point(375, 304),
        new Point(376, 303),
        new Point(377, 302),
        new Point(378, 302),
        new Point(379, 301),
        new Point(380, 300),
        new Point(381, 300),
        new Point(382, 299),
        new Point(383, 299),
        new Point(384, 299),
        new Point(385, 298),
        new Point(386, 297),
        new Point(387, 296),
        new Point(388, 295),
        new Point(389, 294),
        new Point(390, 294),
        new Point(391, 293),
        new Point(392, 293),
        new Point(393, 292),
        new Point(394, 291),
        new Point(395, 290),
        new Point(396, 289),
        new Point(397, 288),
        new Point(398, 287),
        new Point(399, 286),
        new Point(400, 285),
        new Point(401, 284),
        new Point(402, 283),
        new Point(403, 282),
        new Point(404, 281),
        new Point(405, 280),
        new Point(406, 279),
        new Point(407, 278),
        new Point(408, 277),
        new Point(409, 276),
        new Point(409, 275),
        new Point(410, 274),
        new Point(411, 273),
        new Point(412, 272),
        new Point(413, 271),
        new Point(414, 270),
        new Point(415, 269),
        new Point(415, 268),
        new Point(416, 267),
        new Point(416, 266),
        new Point(417, 265),
        new Point(418, 264),
        new Point(418, 263),
        new Point(419, 262),
        new Point(420, 261),
        new Point(420, 260),
        new Point(420, 259),
        new Point(421, 258),
        new Point(421, 257),
        new Point(422, 256),
        new Point(422, 255),
        new Point(422, 254),
        new Point(423, 253),
        new Point(424, 252),
        new Point(424, 251),
        new Point(424, 250),
        new Point(425, 249),
        new Point(426, 248),
        new Point(426, 247),
        new Point(426, 246),
        new Point(426, 245),
        new Point(427, 244),
        new Point(427, 243),
        new Point(428, 242),
        new Point(428, 241),
        new Point(428, 240),
        new Point(429, 239),
        new Point(429, 238),
        new Point(429, 237),
        new Point(429, 236),
        new Point(430, 235),
        new Point(430, 234),
        new Point(431, 233),
        new Point(431, 232),
        new Point(431, 231),
        new Point(431, 230),
        new Point(431, 229),
        new Point(431, 228),
        new Point(432, 227),
        new Point(432, 226),
        new Point(432, 225),
        new Point(432, 224),
        new Point(432, 223),
        new Point(433, 222),
        new Point(433, 221),
        new Point(433, 220),
        new Point(433, 219),
        new Point(433, 218),
        new Point(433, 217),
        new Point(433, 216),
        new Point(434, 215),
        new Point(434, 214),
        new Point(434, 213),
        new Point(434, 212),
        new Point(434, 211),
        new Point(434, 210),
        new Point(435, 209),
        new Point(435, 208),
        new Point(435, 207),
        new Point(435, 206),
        new Point(435, 205),
        new Point(435, 204),
        new Point(435, 203),
        new Point(435, 202),
        new Point(435, 201),
        new Point(435, 200),
        new Point(435, 199),
        new Point(435, 198),
        new Point(435, 197),
        new Point(435, 196),
        new Point(435, 195),
        new Point(435, 194),
        new Point(435, 193),
        new Point(435, 192),
        new Point(435, 191),
        new Point(435, 190),
        new Point(435, 189),
        new Point(435, 188),
        new Point(435, 187),
        new Point(435, 186),
        new Point(435, 185),
        new Point(435, 184),
        new Point(435, 183),
        new Point(435, 182),
        new Point(435, 181),
        new Point(435, 180),
        new Point(435, 179),
        new Point(435, 178),
        new Point(434, 178),
        new Point(434, 177),
        new Point(434, 176),
        new Point(434, 175),
        new Point(434, 174),
        new Point(434, 173),
        new Point(434, 172),
        new Point(434, 171),
        new Point(433, 171),
        new Point(433, 170),
        new Point(433, 169),
        new Point(433, 168),
        new Point(433, 167),
        new Point(433, 166),
        new Point(433, 165),
        new Point(432, 165),
        new Point(432, 164),
        new Point(432, 163),
        new Point(432, 162),
        new Point(432, 161),
        new Point(432, 160),
        new Point(432, 159),
        new Point(431, 159),
        new Point(431, 158),
        new Point(431, 157),
        new Point(431, 156),
        new Point(431, 155),
        new Point(431, 154),
        new Point(430, 154),
        new Point(430, 153),
        new Point(430, 152),
        new Point(430, 151),
        new Point(430, 150),
        new Point(429, 150),
        new Point(429, 149),
        new Point(429, 148),
        new Point(428, 148),
        new Point(428, 147),
        new Point(428, 146),
        new Point(428, 145),
        new Point(428, 144),
        new Point(427, 144),
        new Point(427, 143),
        new Point(427, 142),
        new Point(427, 141),
        new Point(426, 141),
        new Point(426, 140),
        new Point(426, 139),
        new Point(426, 138),
        new Point(425, 138),
        new Point(425, 137),
        new Point(425, 136),
        new Point(425, 135),
        new Point(424, 135),
        new Point(424, 134),
        new Point(423, 134),
        new Point(423, 133),
        new Point(423, 132),
        new Point(422, 132),
        new Point(422, 131),
        new Point(422, 130),
        new Point(422, 129),
        new Point(421, 129),
        new Point(421, 128),
        new Point(421, 127),
        new Point(420, 127),
        new Point(420, 126),
        new Point(420, 125),
        new Point(419, 125),
        new Point(419, 124),
        new Point(418, 124),
        new Point(418, 123),
        new Point(418, 122),
        new Point(417, 122),
        new Point(417, 121),
        new Point(416, 121),
        new Point(416, 120),
        new Point(415, 120),
        new Point(414, 120),
        new Point(414, 119),
        new Point(413, 119),
        new Point(412, 119),
        new Point(411, 119),
        new Point(410, 119),
        new Point(409, 119),
        new Point(408, 119),
        new Point(407, 119),
        new Point(406, 119),
        new Point(405, 119),
        new Point(405, 118),
        new Point(404, 118),
        new Point(403, 118),
        new Point(402, 118),
        new Point(401, 118),
        new Point(400, 118),
        new Point(399, 118),
        new Point(398, 118),
        new Point(397, 118),
        new Point(396, 118),
        new Point(395, 118),
        new Point(394, 118),
        new Point(393, 118),
        new Point(392, 118),
        new Point(391, 118),
        new Point(390, 118),
        new Point(389, 118),
        new Point(388, 118),
        new Point(387, 118),
        new Point(386, 118),
        new Point(385, 118),
        new Point(384, 118),
        new Point(383, 118),
        new Point(382, 118),
        new Point(381, 118),
        new Point(380, 119),
        new Point(379, 119),
        new Point(378, 119),
        new Point(377, 119),
        new Point(376, 119),
        new Point(375, 119),
        new Point(374, 119),
        new Point(373, 119),
        new Point(372, 119),
        new Point(371, 119),
        new Point(370, 120),
        new Point(369, 120),
        new Point(368, 120),
        new Point(367, 120),
        new Point(366, 120),
        new Point(365, 120),
        new Point(364, 120),
        new Point(363, 120),
        new Point(362, 121),
        new Point(361, 121),
        new Point(360, 121),
        new Point(359, 121),
        new Point(358, 121),
        new Point(357, 122),
        new Point(356, 122),
        new Point(355, 122),
        new Point(354, 122),
        new Point(353, 123),
        new Point(352, 124),
        new Point(351, 124),
        new Point(350, 124),
        new Point(349, 125),
        new Point(348, 125),
        new Point(347, 125),
        new Point(346, 125),
        new Point(345, 125),
        new Point(344, 126),
        new Point(343, 126),
        new Point(342, 126),
        new Point(341, 127),
        new Point(340, 127),
        new Point(339, 127),
        new Point(338, 128),
        new Point(337, 129),
        new Point(336, 129),
        new Point(335, 130),
        new Point(334, 130),
        new Point(333, 130),
        new Point(332, 131),
        new Point(331, 131),
        new Point(330, 132),
        new Point(329, 133),
        new Point(328, 133),
        new Point(327, 133),
        new Point(326, 134),
        new Point(325, 134),
        new Point(324, 135),
        new Point(323, 136),
        new Point(322, 136),
        new Point(321, 137),
        new Point(320, 138),
        new Point(319, 138),
        new Point(318, 139),
        new Point(317, 140),
        new Point(316, 140),
        new Point(315, 141),
        new Point(314, 142),
        new Point(313, 142),
        new Point(312, 143),
        new Point(311, 144),
        new Point(310, 145),
        new Point(309, 145),
        new Point(308, 146),
        new Point(307, 147),
        new Point(306, 147),
        new Point(305, 148),
        new Point(304, 149),
        new Point(303, 150),
        new Point(302, 151),
        new Point(301, 152),
        new Point(300, 153),
        new Point(299, 154),
        new Point(298, 155),
        new Point(297, 156),
        new Point(296, 157),
        new Point(295, 158),
        new Point(294, 159),
        new Point(293, 160),
        new Point(292, 161),
        new Point(291, 162),
        new Point(290, 163),
        new Point(289, 164),
        new Point(288, 165),
        new Point(287, 166),
        new Point(286, 167),
        new Point(285, 168),
        new Point(284, 169),
        new Point(284, 170),
        new Point(283, 171),
        new Point(282, 172),
        new Point(281, 173),
        new Point(281, 174),
        new Point(281, 175),
        new Point(280, 176),
        new Point(279, 177),
        new Point(279, 178),
        new Point(278, 179),
        new Point(277, 180),
        new Point(276, 181),
        new Point(276, 182),
        new Point(276, 183),
        new Point(275, 184),
        new Point(274, 185),
        new Point(274, 186),
        new Point(274, 187),
        new Point(273, 188),
        new Point(273, 189),
        new Point(272, 190),
        new Point(272, 191),
        new Point(272, 192),
        new Point(271, 193),
        new Point(270, 194),
        new Point(270, 195),
        new Point(270, 196),
        new Point(269, 197),
        new Point(269, 198),
        new Point(269, 199),
        new Point(268, 200),
        new Point(268, 201)
    };

    private final static Point[] redLeaf = {
        new Point(348, 331),
        new Point(348, 332),
        new Point(348, 333),
        new Point(348, 334),
        new Point(348, 335),
        new Point(348, 336),
        new Point(349, 336),
        new Point(349, 337),
        new Point(349, 338),
        new Point(350, 338),
        new Point(350, 339),
        new Point(351, 339),
        new Point(351, 340),
        new Point(352, 340),
        new Point(353, 340),
        new Point(353, 341),
        new Point(354, 341),
        new Point(355, 341),
        new Point(355, 342),
        new Point(356, 342),
        new Point(357, 342),
        new Point(357, 343),
        new Point(358, 343),
        new Point(359, 343),
        new Point(360, 343),
        new Point(360, 344),
        new Point(361, 344),
        new Point(361, 345),
        new Point(362, 345),
        new Point(363, 345),
        new Point(363, 346),
        new Point(364, 346),
        new Point(365, 346),
        new Point(365, 347),
        new Point(366, 347),
        new Point(367, 347),
        new Point(368, 347),
        new Point(368, 348),
        new Point(369, 348),
        new Point(370, 348),
        new Point(371, 348),
        new Point(371, 349),
        new Point(372, 349),
        new Point(373, 349),
        new Point(373, 350),
        new Point(374, 350),
        new Point(375, 350),
        new Point(375, 351),
        new Point(376, 351),
        new Point(377, 351),
        new Point(377, 352),
        new Point(378, 352),
        new Point(379, 352),
        new Point(380, 352),
        new Point(381, 352),
        new Point(382, 352),
        new Point(382, 353),
        new Point(383, 353),
        new Point(384, 353),
        new Point(385, 353),
        new Point(386, 353),
        new Point(386, 354),
        new Point(387, 354),
        new Point(388, 354),
        new Point(389, 354),
        new Point(390, 354),
        new Point(390, 355),
        new Point(391, 355),
        new Point(392, 355),
        new Point(393, 355),
        new Point(393, 356),
        new Point(394, 356),
        new Point(395, 356),
        new Point(396, 356),
        new Point(397, 356),
        new Point(398, 356),
        new Point(399, 356),
        new Point(399, 357),
        new Point(400, 357),
        new Point(401, 357),
        new Point(402, 357),
        new Point(403, 357),
        new Point(404, 357),
        new Point(405, 357),
        new Point(406, 357),
        new Point(407, 357),
        new Point(408, 357),
        new Point(409, 357),
        new Point(410, 357),
        new Point(411, 357),
        new Point(412, 357),
        new Point(413, 357),
        new Point(414, 357),
        new Point(415, 357),
        new Point(416, 357),
        new Point(417, 357),
        new Point(418, 357),
        new Point(419, 357),
        new Point(420, 357),
        new Point(421, 357),
        new Point(422, 357),
        new Point(423, 356),
        new Point(424, 356),
        new Point(425, 356),
        new Point(426, 356),
        new Point(427, 356),
        new Point(428, 356),
        new Point(429, 356),
        new Point(430, 355),
        new Point(431, 355),
        new Point(432, 355),
        new Point(433, 354),
        new Point(434, 354),
        new Point(435, 354),
        new Point(436, 354),
        new Point(437, 354),
        new Point(438, 353),
        new Point(439, 353),
        new Point(440, 352),
        new Point(441, 352),
        new Point(442, 352),
        new Point(443, 352),
        new Point(444, 352),
        new Point(445, 351),
        new Point(446, 351),
        new Point(447, 351),
        new Point(448, 350),
        new Point(449, 350),
        new Point(450, 349),
        new Point(451, 348),
        new Point(452, 348),
        new Point(453, 347),
        new Point(454, 347),
        new Point(455, 346),
        new Point(456, 346),
        new Point(457, 346),
        new Point(458, 346),
        new Point(459, 345),
        new Point(460, 344),
        new Point(461, 343),
        new Point(462, 343),
        new Point(463, 343),
        new Point(464, 342),
        new Point(465, 341),
        new Point(466, 340),
        new Point(467, 340),
        new Point(468, 340),
        new Point(469, 339),
        new Point(470, 338),
        new Point(471, 337),
        new Point(472, 336),
        new Point(473, 336),
        new Point(474, 335),
        new Point(475, 334),
        new Point(476, 334),
        new Point(477, 333),
        new Point(478, 332),
        new Point(479, 331),
        new Point(480, 330),
        new Point(481, 329),
        new Point(482, 328),
        new Point(483, 327),
        new Point(484, 326),
        new Point(485, 325),
        new Point(486, 324),
        new Point(487, 323),
        new Point(488, 322),
        new Point(489, 321),
        new Point(490, 320),
        new Point(491, 319),
        new Point(492, 318),
        new Point(493, 317),
        new Point(494, 316),
        new Point(495, 315),
        new Point(496, 314),
        new Point(497, 313),
        new Point(498, 312),
        new Point(499, 311),
        new Point(500, 310),
        new Point(501, 309),
        new Point(501, 308),
        new Point(502, 307),
        new Point(503, 306),
        new Point(504, 305),
        new Point(505, 304),
        new Point(506, 303),
        new Point(506, 302),
        new Point(507, 301),
        new Point(507, 300),
        new Point(508, 299),
        new Point(509, 298),
        new Point(510, 297),
        new Point(511, 296),
        new Point(511, 295),
        new Point(511, 294),
        new Point(512, 293),
        new Point(512, 292),
        new Point(513, 291),
        new Point(514, 290),
        new Point(515, 289),
        new Point(515, 288),
        new Point(515, 287),
        new Point(516, 286),
        new Point(517, 285),
        new Point(517, 284),
        new Point(517, 283),
        new Point(518, 282),
        new Point(518, 281),
        new Point(519, 280),
        new Point(520, 279),
        new Point(521, 278),
        new Point(521, 277),
        new Point(521, 276),
        new Point(521, 275),
        new Point(522, 274),
        new Point(523, 273),
        new Point(523, 272),
        new Point(523, 271),
        new Point(523, 270),
        new Point(524, 269),
        new Point(524, 268),
        new Point(525, 267),
        new Point(525, 266),
        new Point(525, 265),
        new Point(526, 264),
        new Point(526, 263),
        new Point(527, 262),
        new Point(527, 261),
        new Point(527, 260),
        new Point(527, 259),
        new Point(528, 258),
        new Point(528, 257),
        new Point(528, 256),
        new Point(528, 255),
        new Point(528, 254),
        new Point(529, 253),
        new Point(529, 252),
        new Point(529, 251),
        new Point(529, 250),
        new Point(529, 249),
        new Point(529, 248),
        new Point(529, 247),
        new Point(529, 246),
        new Point(528, 246),
        new Point(528, 245),
        new Point(527, 245),
        new Point(527, 244),
        new Point(526, 244),
        new Point(526, 243),
        new Point(525, 243),
        new Point(525, 242),
        new Point(524, 242),
        new Point(524, 241),
        new Point(523, 241),
        new Point(523, 240),
        new Point(523, 239),
        new Point(522, 239),
        new Point(522, 238),
        new Point(521, 238),
        new Point(520, 238),
        new Point(520, 237),
        new Point(519, 237),
        new Point(519, 236),
        new Point(518, 236),
        new Point(518, 235),
        new Point(517, 235),
        new Point(517, 234),
        new Point(516, 234),
        new Point(516, 233),
        new Point(515, 233),
        new Point(515, 232),
        new Point(514, 232),
        new Point(514, 231),
        new Point(513, 231),
        new Point(512, 231),
        new Point(512, 230),
        new Point(511, 230),
        new Point(511, 229),
        new Point(510, 229),
        new Point(510, 228),
        new Point(509, 228),
        new Point(509, 227),
        new Point(508, 227),
        new Point(507, 227),
        new Point(507, 226),
        new Point(506, 226),
        new Point(506, 225),
        new Point(505, 225),
        new Point(505, 224),
        new Point(504, 224),
        new Point(503, 224),
        new Point(503, 223),
        new Point(502, 223),
        new Point(502, 222),
        new Point(501, 222),
        new Point(500, 222),
        new Point(500, 221),
        new Point(499, 221),
        new Point(499, 220),
        new Point(498, 220),
        new Point(497, 220),
        new Point(497, 219),
        new Point(496, 219),
        new Point(496, 218),
        new Point(495, 218),
        new Point(494, 218),
        new Point(494, 217),
        new Point(493, 217),
        new Point(492, 217),
        new Point(492, 216),
        new Point(491, 216),
        new Point(490, 216),
        new Point(490, 215),
        new Point(489, 215),
        new Point(488, 215),
        new Point(488, 214),
        new Point(487, 214),
        new Point(487, 213),
        new Point(486, 213),
        new Point(485, 213),
        new Point(485, 212),
        new Point(484, 212),
        new Point(483, 212),
        new Point(483, 211),
        new Point(482, 211),
        new Point(481, 211),
        new Point(480, 211),
        new Point(480, 210),
        new Point(479, 210),
        new Point(478, 210),
        new Point(478, 209),
        new Point(477, 209),
        new Point(476, 209),
        new Point(476, 208),
        new Point(475, 208),
        new Point(474, 208),
        new Point(474, 207),
        new Point(473, 207),
        new Point(472, 207),
        new Point(471, 207),
        new Point(471, 206),
        new Point(470, 206),
        new Point(469, 206),
        new Point(468, 206),
        new Point(468, 205),
        new Point(467, 205),
        new Point(466, 205),
        new Point(465, 205),
        new Point(465, 204),
        new Point(464, 204),
        new Point(463, 204),
        new Point(462, 204),
        new Point(461, 204),
        new Point(461, 203),
        new Point(460, 203),
        new Point(459, 203),
        new Point(459, 202),
        new Point(458, 202),
        new Point(457, 202),
        new Point(456, 202),
        new Point(455, 202),
        new Point(454, 202),
        new Point(453, 202),
        new Point(452, 203),
        new Point(451, 204),
        new Point(450, 205),
        new Point(449, 206),
        new Point(449, 207),
        new Point(449, 208),
        new Point(449, 209),
        new Point(449, 210),
        new Point(449, 211),
        new Point(449, 212),
        new Point(449, 213),
        new Point(449, 214),
        new Point(449, 215),
        new Point(449, 216),
        new Point(449, 217),
        new Point(449, 218),
        new Point(449, 219),
        new Point(448, 220),
        new Point(448, 221),
        new Point(448, 222),
        new Point(448, 223),
        new Point(448, 224),
        new Point(447, 225),
        new Point(447, 226),
        new Point(447, 227),
        new Point(447, 228),
        new Point(446, 229),
        new Point(446, 230),
        new Point(446, 231),
        new Point(446, 232),
        new Point(445, 233),
        new Point(445, 234),
        new Point(445, 235),
        new Point(445, 236),
        new Point(444, 237),
        new Point(444, 238),
        new Point(443, 239),
        new Point(443, 240),
        new Point(442, 241),
        new Point(442, 242),
        new Point(441, 243),
        new Point(441, 244),
        new Point(441, 245),
        new Point(441, 246),
        new Point(440, 247),
        new Point(439, 248),
        new Point(439, 249),
        new Point(439, 250),
        new Point(438, 251),
        new Point(438, 252),
        new Point(438, 253),
        new Point(438, 254),
        new Point(438, 255),
        new Point(439, 255),
        new Point(439, 256),
        new Point(439, 257),
        new Point(440, 257),
        new Point(440, 258),
        new Point(441, 258),
        new Point(442, 258),
        new Point(442, 259),
        new Point(443, 259),
        new Point(444, 259),
        new Point(445, 259),
        new Point(446, 259),
        new Point(447, 259),
        new Point(448, 259),
        new Point(449, 259),
        new Point(450, 259),
        new Point(451, 259),
        new Point(452, 259),
        new Point(453, 259),
        new Point(454, 259),
        new Point(455, 259),
        new Point(456, 259),
        new Point(457, 259),
        new Point(458, 259),
        new Point(459, 259),
        new Point(460, 259),
        new Point(461, 259),
        new Point(462, 259),
        new Point(462, 260),
        new Point(463, 260),
        new Point(464, 260),
        new Point(465, 260),
        new Point(465, 261),
        new Point(464, 262),
        new Point(463, 263),
        new Point(462, 263),
        new Point(461, 263),
        new Point(460, 264),
        new Point(459, 264),
        new Point(458, 264),
        new Point(457, 264),
        new Point(456, 265),
        new Point(455, 265),
        new Point(454, 265),
        new Point(453, 266),
        new Point(452, 266),
        new Point(451, 266),
        new Point(450, 266),
        new Point(449, 267),
        new Point(448, 268),
        new Point(447, 268),
        new Point(446, 269),
        new Point(445, 269),
        new Point(444, 269),
        new Point(443, 269),
        new Point(442, 270),
        new Point(441, 270),
        new Point(440, 270),
        new Point(439, 270),
        new Point(438, 270),
        new Point(437, 271),
        new Point(436, 271),
        new Point(435, 271),
        new Point(434, 272),
        new Point(433, 272),
        new Point(432, 273),
        new Point(431, 274),
        new Point(430, 274),
        new Point(429, 275),
        new Point(428, 276),
        new Point(427, 277),
        new Point(426, 278),
        new Point(426, 279),
        new Point(425, 280),
        new Point(425, 281),
        new Point(424, 282),
        new Point(424, 283),
        new Point(423, 284),
        new Point(422, 285),
        new Point(422, 286),
        new Point(422, 287),
        new Point(421, 288),
        new Point(420, 289),
        new Point(419, 290),
        new Point(418, 291),
        new Point(417, 292),
        new Point(416, 293),
        new Point(415, 294),
        new Point(414, 295),
        new Point(413, 296),
        new Point(412, 297),
        new Point(411, 298),
        new Point(410, 299),
        new Point(409, 300),
        new Point(408, 301),
        new Point(407, 302),
        new Point(406, 303),
        new Point(405, 304),
        new Point(404, 304),
        new Point(403, 305),
        new Point(402, 306),
        new Point(401, 307),
        new Point(400, 307),
        new Point(399, 307),
        new Point(398, 308),
        new Point(397, 309),
        new Point(396, 310),
        new Point(395, 310),
        new Point(394, 311),
        new Point(393, 311),
        new Point(392, 312),
        new Point(391, 313),
        new Point(390, 313),
        new Point(389, 313),
        new Point(388, 314),
        new Point(387, 314),
        new Point(386, 315),
        new Point(385, 316),
        new Point(384, 316),
        new Point(383, 317),
        new Point(382, 317),
        new Point(381, 318),
        new Point(380, 318),
        new Point(379, 318),
        new Point(378, 319),
        new Point(377, 319),
        new Point(376, 319),
        new Point(375, 319),
        new Point(374, 320),
        new Point(373, 321),
        new Point(372, 321),
        new Point(371, 322),
        new Point(370, 322),
        new Point(369, 322),
        new Point(368, 323),
        new Point(367, 323),
        new Point(366, 323),
        new Point(365, 324),
        new Point(364, 324),
        new Point(363, 324),
        new Point(362, 324),
        new Point(361, 325),
        new Point(360, 325),
        new Point(359, 325),
        new Point(358, 325),
        new Point(357, 326),
        new Point(356, 327),
        new Point(355, 327),
        new Point(354, 327),
        new Point(353, 328),
        new Point(352, 328),
        new Point(351, 329),
        new Point(350, 329)
    };

    public static Polygon getMainLeaf(Double scalex, double scaley) {

        return polygonizeControlPoints(blackLeaf, scalex, scaley);
    }

    static Polygon polygonizeControlPoints(Point[] pp, double scalex, double scaley) {
        Polygon r = new Polygon();
        for (int i = 0; i < pp.length; i++) {
            Point p = pp[i];
            //small movement to right
            r.addPoint((int) ((double) (p.x - 17) * scalex), (int) ((double) p.y * scaley));
        }
        return r;
    }

    public static Polygon getSecondLeaf(double scalex, double scaley) {
        return polygonizeControlPoints(redLeaf, scalex, scaley);
    }

//    public static Polygon getMainLeafCurve(Double scalex, double scaley) {
//        return getNatCubicClosed(getMainLeaf(scalex, scaley));
//    }
//
//  
//    public static Polygon getSecondLeafCurve(Double scalex, double scaley) {
//        return getNatCubicClosed(getSecondLeaf(scalex, scaley));
//    }
    static Polygon getNatCubicClosed(Polygon p) {
        NatCubicClosed c = new NatCubicClosed();
        c.setSourcePolygon(p);
        return c.calculateResult();
    }

    /**
     * Small program to vectorize leaves
     *
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        //File f = new File("/home/jvanek/hg/icedtea-web/netx/redLeaf.png");
        File f = new File("/home/jvanek/hg/icedtea-web/netx/blackLeaf.png");
        File fOut = new File(f.getAbsolutePath().replace(".png", "_result.png"));
        BufferedImage maze = ImageIO.read(f);
        //for other sources then my current leaves, there must be empty space between start and end, otherwise shortest path will fail
        //start for blackLeaf is [267,264] end [268,202]
        //start for redLeaf is [348,331] end [349,329]
        Map<Node, Node> allNodes = new HashMap<Node, Node>();
        Node start = null;
        //first init data
        for (int x = 0; x < maze.getWidth(); x++) {
            for (int y = 0; y < maze.getHeight(); y++) {
                Color c = new Color(maze.getRGB(x, y), true);
                if (c.getAlpha() > 0) {
                    Node n = new Node(x, y);
                    Node nn = allNodes.get(n);
                    if (nn == null) {
                        allNodes.put(n, n);
                    } else {
                        n = nn;
                    }
                    if (start == null) {
                        start = n;
                    }
                    //check closest neighbourhood
                    for (int xx = -1; xx <= 1; xx++) {
                        for (int yy = -1; yy <= 1; yy++) {
                            if (xx == yy) {
                                continue;
                            }
                            int xxx = x + xx;
                            int yyy = y + yy;
                            //all non-transparent neighbours are neighbours with distance of 1
                            Color ccc = new Color(maze.getRGB(xxx, yyy), true);
                            if (ccc.getAlpha() > 0) {
                                Node n2 = new Node(xxx, yyy);
                                Node nn2 = allNodes.get(n2);
                                if (nn2 == null) {
                                    allNodes.put(n2, n2);
                                } else {
                                    n2 = nn2;
                                }
                                n.addDestination(n2, 1);
                            }
                        }
                    }
                }
            }
        }
        //calculate path from start to mnsot far away point (that's why the empty space)
        Collection<Node> nodesCol = allNodes.values();
        Graph g = new Graph();
        for (Node node : nodesCol) {
            g.addNode(node);
        }

        Graph graph = calculateShortestPathFromSource(g, start);
        List<Node> result = new ArrayList<>(graph.nodes.size());
        result.addAll(graph.nodes);
        Collections.sort(result, new Comparator<Node>() {
            @Override
            public int compare(Node o1, Node o2) {
                return o1.distance - o2.distance;
            }
        });
        Node finalNode = result.get(result.size() - 1);
        List<Node> path = finalNode.getShortestPath();
        //view result if needed
        BufferedImage finish = new BufferedImage(maze.getWidth(), maze.getHeight(), maze.getType());
        System.out.println("private final static Point[] " + f.getName() + " = {");

        Graphics2D g2d = finish.createGraphics();
        for (int i = 1; i < path.size(); i++) {
            Node from = path.get(i - 1);
            Node to = path.get(i);
            g2d.drawLine(from.x, from.y, to.x, to.y);
            System.out.println("                    new Point(" + from.x + ", " + from.y + "),");

        }
        Node from = path.get(path.size() - 1);
        Node to = path.get(0);
        System.out.println("                    new Point(" + from.x + ", " + from.y + ")");
        g2d.drawLine(from.x, from.y, to.x, to.y);
        System.out.println("    };");
        //ImageIO.write(finish, "png", fOut);
    }

    public static class Graph {

        private Set<Node> nodes = new HashSet<>();

        public void addNode(Node nodeA) {
            nodes.add(nodeA);
        }

        // getters and setters 
    }

    public static class Node {

        private final String name;

        private List<Node> shortestPath = new LinkedList<>();

        private Integer distance = Integer.MAX_VALUE;

        Map<Node, Integer> adjacentNodes = new HashMap<>();
        private final int x;
        private final int y;

        public void addDestination(Node destination, int distance) {
            adjacentNodes.put(destination, distance);
        }

        public Node(int xOrig, int yOrig) {
            this.name = xOrig + " x " + yOrig;
            this.x = xOrig;
            this.y = yOrig;
        }

        public Integer getDistance() {
            return distance;
        }

        public void setDistance(Integer distance) {
            this.distance = distance;
        }

        public Map<Node, Integer> getAdjacentNodes() {
            return adjacentNodes;
        }

        public void setShortestPath(List<Node> shortestPath) {
            this.shortestPath = shortestPath;
        }

        public List<Node> getShortestPath() {
            return shortestPath;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Node) {
                return (((Node) obj).name.equals(this.name));
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }
    }

    public static Graph calculateShortestPathFromSource(Graph graph, Node source) {
        source.setDistance(0);

        Set<Node> settledNodes = new HashSet<>();
        Set<Node> unsettledNodes = new HashSet<>();

        unsettledNodes.add(source);

        while (!unsettledNodes.isEmpty()) {
            Node currentNode = getLowestDistanceNode(unsettledNodes);
            unsettledNodes.remove(currentNode);
            for (Entry< Node, Integer> adjacencyPair
                    : currentNode.getAdjacentNodes().entrySet()) {
                Node adjacentNode = adjacencyPair.getKey();
                Integer edgeWeight = adjacencyPair.getValue();
                if (!settledNodes.contains(adjacentNode)) {
                    calculateMinimumDistance(adjacentNode, edgeWeight, currentNode);
                    unsettledNodes.add(adjacentNode);
                }
            }
            settledNodes.add(currentNode);
        }
        return graph;
    }

    private static Node getLowestDistanceNode(Set< Node> unsettledNodes) {
        Node lowestDistanceNode = null;
        int lowestDistance = Integer.MAX_VALUE;
        for (Node node : unsettledNodes) {
            int nodeDistance = node.getDistance();
            if (nodeDistance < lowestDistance) {
                lowestDistance = nodeDistance;
                lowestDistanceNode = node;
            }
        }
        return lowestDistanceNode;
    }

    private static void calculateMinimumDistance(Node evaluationNode,
            Integer edgeWeigh, Node sourceNode) {
        Integer sourceDistance = sourceNode.getDistance();
        if (sourceDistance + edgeWeigh < evaluationNode.getDistance()) {
            evaluationNode.setDistance(sourceDistance + edgeWeigh);
            LinkedList<Node> shortestPath = new LinkedList<>(sourceNode.getShortestPath());
            shortestPath.add(sourceNode);
            evaluationNode.setShortestPath(shortestPath);
        }
    }
}
